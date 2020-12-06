package edu.teco.earablecompanion.overview.device.cosinuss

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
import edu.teco.earablecompanion.databinding.CosinussDeviceFragmentBinding
import edu.teco.earablecompanion.overview.device.DeviceFragment

@AndroidEntryPoint
class CosinussDeviceFragment : DeviceFragment() {

    private val viewModel: CosinussDeviceViewModel by viewModels()
    private val navController: NavController by lazy { findNavController() }
    private val args: CosinussDeviceFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = CosinussDeviceFragmentBinding.inflate(inflater, container, false).apply {
            vm = viewModel
            lifecycleOwner = this@CosinussDeviceFragment

            switchHeartRate.setOnCheckedChangeListener { _, isChecked -> viewModel.setHeartRateEnabled(isChecked) }
            switchBodyTemperature.setOnCheckedChangeListener { _, isChecked -> viewModel.setBodyTemperatureEnabled(isChecked) }
            switchAccelerometer.setOnCheckedChangeListener { _, isChecked -> viewModel.setAccelerometerEnabled(isChecked) }
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun disconnectDevice() {
        (activity as? MainActivity)?.earableService?.disconnect(args.device)
        navController.popBackStack()
    }
}