package edu.teco.earablecompanion.overview.device.generic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import edu.teco.earablecompanion.MainActivity
import edu.teco.earablecompanion.databinding.GenericDeviceFragmentBinding
import edu.teco.earablecompanion.overview.device.DeviceFragment

@AndroidEntryPoint
class GenericDeviceFragment : DeviceFragment() {

    private val viewModel: GenericDeviceViewModel by viewModels()
    private val navController: NavController by lazy { findNavController() }
    private val args: GenericDeviceFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = GenericDeviceFragmentBinding.inflate(inflater, container, false).apply {
            vm = viewModel
            lifecycleOwner = this@GenericDeviceFragment

            genericHeartRateEnabledSwitch.setOnCheckedChangeListener { _, isChecked -> viewModel.setHeartRateEnabled(isChecked) }
            genericBodyTemperatureEnabledSwitch.setOnCheckedChangeListener { _, isChecked -> viewModel.setBodyTemperatureEnabled(isChecked) }
            genericOximeterEnabledSwitch.setOnCheckedChangeListener { _, isChecked -> viewModel.setOximeterEnabled(isChecked) }
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun disconnectDevice() {
        (activity as? MainActivity)?.earableService?.disconnect(args.device)
        navController.popBackStack()
    }
}