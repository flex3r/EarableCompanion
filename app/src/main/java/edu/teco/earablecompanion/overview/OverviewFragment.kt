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
import edu.teco.earablecompanion.overview.values.ValuesFragment
import edu.teco.earablecompanion.utils.extensions.showShortSnackbar
import edu.teco.earablecompanion.utils.extensions.valueOrFalse

@AndroidEntryPoint
class OverviewFragment : Fragment() {

    private val viewModel: OverviewViewModel by viewModels()
    private val navController: NavController by lazy { findNavController() }
    private val sharedPreferences: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }
    private var bindingRef: OverviewFragmentBinding? = null
    private val binding get() = bindingRef!!

    private val requestPermissionRegistration = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
        when {
            result -> displayStartRecordingDialog()
            else -> binding.root.showShortSnackbar(getString(R.string.audio_permission_disclaimer))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val adapter = OverviewAdapter(::showConnectionBottomSheet, ::disconnectDevice, ::showValuesDialog, ::calibrateDevice, ::setMicEnabled) { device ->
            if (viewModel.isRecording.valueOrFalse) return@OverviewAdapter

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
        }

        bindingRef = OverviewFragmentBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@OverviewFragment
            vm = viewModel
            recyclerDevices.adapter = adapter
            fabRecord.setOnClickListener {
                when {
                    viewModel.isRecording.valueOrFalse -> stopRecording()
                    else -> requestRecordingPermissionIfNeeded()
                }
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bindingRef = null
    }

    fun onConnected(event: ConnectionEvent.Connected) {
        binding.root.showShortSnackbar(getString(R.string.connection_snackbar_text_connected, event.device.name ?: getString(R.string.unknown_device_name))) {
            if (event.config.canCalibrate) {
                setAction(R.string.calibrate) { showCalibrationBottomSheet(event.device) }
            }
        }
    }

    fun onConnectionFailed() {
        binding.root.showShortSnackbar(getString(R.string.connection_snackbar_text_failed))
    }

    private fun showValuesDialog() {
        val dialog = ValuesFragment()
        dialog.show(childFragmentManager, ValuesFragment::class.java.simpleName)
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

    private fun requestRecordingPermissionIfNeeded() = when {
        viewModel.micRecordingPossible -> requestPermissionRegistration.launch(android.Manifest.permission.RECORD_AUDIO)
        else -> displayStartRecordingDialog()
    }

    private fun displayStartRecordingDialog() {
        val labelKey = getString(R.string.preference_recording_labels_key)
        val default = getString(R.string.preference_recording_labels_default)
        val other = getString(R.string.label_other)
        val labelString = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString(labelKey, default) ?: default
        val labels = labelString.split(",").plus(other).toTypedArray()
        var index = 0

        MaterialAlertDialogBuilder(requireContext())
            .setSingleChoiceItems(labels, index) { _, idx -> index = idx }
            .setTitle(R.string.start_recording_dialog_title)
            .setPositiveButton(R.string.start_recording_dialog_positive) { _, _ ->
                when (index) {
                    labels.lastIndex -> showCustomTitleDialog()
                    else -> {
                        val title = labels.getOrNull(index) ?: getString(R.string.start_recording_dialog_title_default)
                        startRecording(title)
                    }
                }
            }
            .setNegativeButton(R.string.start_recording_dialog_negative) { d, _ -> d.dismiss() }
            .show()
    }

    private fun showCustomTitleDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val layout = LayoutInflater.from(builder.context).inflate(R.layout.dialog_input_layout, null) as LinearLayout
        layout.findViewById<TextInputLayout>(R.id.dialog_input_layout).apply {
            hint = getString(R.string.start_recording_dialog_hint)
        }
        val editText = layout.findViewById<TextInputEditText>(R.id.dialog_input_text).apply {
            isSingleLine = true
        }

        builder
            .setTitle(R.string.start_recording_dialog_title)
            .setView(layout)
            .setPositiveButton(R.string.start_recording_dialog_positive) { _, _ ->
                val input = editText.text?.toString()
                val title = when {
                    input.isNullOrBlank() -> getString(R.string.start_recording_dialog_title_default)
                    else -> input
                }
                startRecording(title)
            }
            .setNegativeButton(R.string.start_recording_dialog_negative) { d, _ -> d.dismiss() }
            .show()
    }

    private fun startRecording(title: String) {
        val devices = viewModel.overviewItems.value?.filterIsInstance<OverviewItem.Device>()?.map { it.bluetoothDevice }
        val configs = viewModel.getCurrentConfigs()
        devices ?: return

        (activity as? MainActivity)?.run {
            with(earableService ?: return) {
                val shouldInterceptMediaButtons = sharedPreferences.getBoolean(getString(R.string.preference_intercept_media_buttons_key), false)
                val session = startRecording(title, devices, configs, viewModel.micRecordingPossible, shouldInterceptMediaButtons) ?: return

                mediaController = session.controller.mediaController as MediaController
            }
        }
    }

    private fun stopRecording() {
        val devices = viewModel.overviewItems.value?.filterIsInstance<OverviewItem.Device>()?.map { it.bluetoothDevice }
        val configs = viewModel.getCurrentConfigs()
        devices ?: return

        (activity as? MainActivity)?.run {
            earableService?.stopRecording(devices, configs)
            mediaController = null
        }
    }

    companion object {
        private val TAG = OverviewFragment::class.java.simpleName
    }
}