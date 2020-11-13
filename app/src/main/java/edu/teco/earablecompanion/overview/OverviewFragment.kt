package edu.teco.earablecompanion.overview

import android.bluetooth.BluetoothDevice
import android.content.SharedPreferences
import android.media.session.MediaController
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import edu.teco.earablecompanion.MainActivity
import edu.teco.earablecompanion.R
import edu.teco.earablecompanion.bluetooth.EarableType
import edu.teco.earablecompanion.databinding.OverviewFragmentBinding
import edu.teco.earablecompanion.overview.calibration.CalibrationFragment
import edu.teco.earablecompanion.overview.connection.ConnectionEvent
import edu.teco.earablecompanion.overview.connection.ConnectionFragment
import edu.teco.earablecompanion.utils.extensions.showOrHide

@AndroidEntryPoint
class OverviewFragment : Fragment() {

    private val viewModel: OverviewViewModel by viewModels()
    private val navController: NavController by lazy { findNavController() }
    private val sharedPreferences: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }
    private lateinit var binding: OverviewFragmentBinding

    private val requestPermissionRegistration = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
        when {
            result -> displayStartRecordingDialog()
            else -> showSnackbar(getString(R.string.audio_permission_disclaimer))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val adapter = OverviewAdapter(::disconnectDevice, ::calibrateDevice, ::setMicEnabled) { device ->
            val action = when (device.type) {
                is EarableType.ESense -> OverviewFragmentDirections.actionOverviewFragmentToESenseDeviceFragment(
                    name = device.name ?: getString(R.string.unknown_esense_device_name),
                    device = device.bluetoothDevice
                )
                is EarableType.Cosinuss -> OverviewFragmentDirections.actionOverviewFragmentToCosinussDeviceFragment(
                    name = device.name ?: getString(R.string.unknown_cosinuss_device_name),
                    device = device.bluetoothDevice
                )
                is EarableType.Generic -> OverviewFragmentDirections.actionOverviewFragmentToGenericDeviceFragment(
                    name = device.name ?: getString(R.string.unknown_device_name),
                    device = device.bluetoothDevice
                )
                else -> null
            }
            action?.let { navController.navigate(it) }
        }

        viewModel.apply {
            overviewItems.observe(viewLifecycleOwner) { adapter.submitList(it) }
            connectedDevicesAndRecording.observe(viewLifecycleOwner) { (hasConnectedDevices, isRecording) ->
                // show when no connected devices && not recording
                binding.connectFab.showOrHide(!hasConnectedDevices && !isRecording)
                // show when connected devices && not recording
                binding.recordFab.showOrHide(hasConnectedDevices && !isRecording)
                binding.connectFabSmall.showOrHide(hasConnectedDevices && !isRecording)
                // show when recording
                binding.stopRecordFab.showOrHide(isRecording)
            }
        }

        binding = OverviewFragmentBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@OverviewFragment
            vm = viewModel
            devicesRecyclerview.adapter = adapter
            connectFab.setOnClickListener { showConnectionBottomSheet() }
            connectFabSmall.setOnClickListener { showConnectionBottomSheet() }
            recordFab.setOnClickListener { requestPermissionIfNeeded() }
            stopRecordFab.setOnClickListener { stopRecording() }
        }

        return binding.root
    }

    fun onConnected(event: ConnectionEvent.Connected) {
        showSnackbar(getString(R.string.connection_snackbar_text_connected, event.device.name ?: getString(R.string.unknown_device_name))) {
            if (event.config.canCalibrate) {
                setAction(R.string.calibrate) { showCalibrationBottomSheet(event.device) }
            }
        }
    }

    fun onConnectionFailed() {
        showSnackbar(getString(R.string.connection_snackbar_text_failed))
    }

    private fun showSnackbar(text: String, block: Snackbar.() -> Unit = {}) {
        Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT)
            .apply(block)
            .show()
    }

    private fun calibrateDevice(item: OverviewItem.Device) {
        showCalibrationBottomSheet(item.bluetoothDevice)
    }

    private fun disconnectDevice(item: OverviewItem.Device) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.remove_device_dialog_title))
            .setPositiveButton(getString(R.string.remove)) { _, _ -> (activity as? MainActivity)?.earableService?.disconnect(item.bluetoothDevice) }
            .setNegativeButton(getString(R.string.cancel)) { d, _ -> d.dismiss() }
            .show()
    }

    private fun setMicEnabled(enabled: Boolean) {
        if (enabled) (activity as? MainActivity)?.earableService?.connectSco()
        viewModel.setMicEnabled(enabled)
    }

    private fun showConnectionBottomSheet() {
        val dialog = ConnectionFragment()
        dialog.show(childFragmentManager, ConnectionFragment::class.java.simpleName)
    }

    private fun showCalibrationBottomSheet(device: BluetoothDevice) {
        val dialog = CalibrationFragment.newInstance(device)
        dialog.show(childFragmentManager, CalibrationFragment::class.java.simpleName)
    }

    private fun requestPermissionIfNeeded() = when {
        viewModel.micRecordingPossible -> requestPermissionRegistration.launch(android.Manifest.permission.RECORD_AUDIO)
        else -> displayStartRecordingDialog()
    }

    private fun displayStartRecordingDialog() {
        val labelKey = getString(R.string.preference_recording_labels_key)
        val default = getString(R.string.preference_recording_labels_default)
        val labelString = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString(labelKey, default) ?: default
        val labels = labelString.split(",").toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setItems(labels) { _, index ->
                val title = labels.getOrNull(index) ?: getString(R.string.start_recording_dialog_title_default)
                startRecording(title)
            }
            .setTitle(R.string.start_recording_dialog_title)
            .setNegativeButton(R.string.start_recording_dialog_negative) { d, _ -> d.dismiss() }
            .show()
    }

    private fun startRecording(title: String) {
        val devices = viewModel.overviewItems.value?.filterIsInstance<OverviewItem.Device>()?.map { it.bluetoothDevice }
        val configs = viewModel.getCurrentConfigs()
        devices?.let {
            (activity as? MainActivity)?.run {
                with(earableService ?: return) {
                    startRecording(title, devices, configs, viewModel.micRecordingPossible)

                    if (sharedPreferences.getBoolean(getString(R.string.preference_intercept_media_buttons_key), false)) {
                        val session = earableService?.startMediaSession() ?: return
                        mediaController = session.controller.mediaController as MediaController
                    }
                }
            }
        }
    }

    private fun stopRecording() {
        val devices = viewModel.overviewItems.value?.filterIsInstance<OverviewItem.Device>()?.map { it.bluetoothDevice }
        val configs = viewModel.getCurrentConfigs()
        devices?.let {
            (activity as? MainActivity)?.earableService?.stopRecording(devices, configs)
            activity?.mediaController = null
        }
    }

    companion object {
        private val TAG = OverviewFragment::class.java.simpleName
    }
}