package edu.teco.earablecompanion.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import edu.teco.earablecompanion.MainActivity
import edu.teco.earablecompanion.bluetooth.earable.EarableType
import edu.teco.earablecompanion.databinding.OverviewFragmentBinding
import edu.teco.earablecompanion.overview.connection.ConnectionFragment
import edu.teco.earablecompanion.utils.showOrHide

@AndroidEntryPoint
class OverviewFragment : Fragment() {

    private val viewModel: OverviewViewModel by viewModels()
    private val navController: NavController by lazy { findNavController() }
    private lateinit var binding: OverviewFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val adapter = OverviewAdapter { device ->
            val action = when (device.type) {
                EarableType.ESENSE -> OverviewFragmentDirections.actionOverviewFragmentToESenseDeviceFragment(device.name, device.bluetoothDevice)
                else -> null // TODO
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
            connectFab.setOnClickListener(::showConnectionBottomSheet)
            connectFabSmall.setOnClickListener(::showConnectionBottomSheet)
            recordFab.setOnClickListener { startRecording() }
            stopRecordFab.setOnClickListener { stopRecording() }
        }

        return binding.root
    }

    fun onCancelConnectionBottomSheet() {
        viewModel.setConnectionOpen(false)
    }

    fun showSnackbar(text: String) = Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()

    private fun showConnectionBottomSheet(view: View?) {
        viewModel.setConnectionOpen(true)
        val dialog = ConnectionFragment()
        dialog.show(childFragmentManager, ConnectionFragment::class.java.simpleName)
    }

    private fun startRecording() {
        val devices = viewModel.overviewItems.value?.filterIsInstance<OverviewItem.Device>()?.map { it.bluetoothDevice }
        val configs = viewModel.getCurrentConfigs()
        devices?.let {
            (activity as? MainActivity)?.earableService?.startRecording(devices, configs)
        }
    }

    private fun stopRecording() {
        (activity as? MainActivity)?.earableService?.stopRecording()
    }

    companion object {
        private val TAG = OverviewFragment::class.java.simpleName
    }
}