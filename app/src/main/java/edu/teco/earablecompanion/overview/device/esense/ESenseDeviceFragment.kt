package edu.teco.earablecompanion.overview.device.esense

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import edu.teco.earablecompanion.MainActivity
import edu.teco.earablecompanion.R
import edu.teco.earablecompanion.bluetooth.earable.ESenseConfig
import edu.teco.earablecompanion.databinding.EsenseDeviceFragmentBinding
import edu.teco.earablecompanion.overview.device.DeviceFragment

@AndroidEntryPoint
class ESenseDeviceFragment : DeviceFragment() {

    private val viewModel: ESenseDeviceViewModel by viewModels()
    private val navController: NavController by lazy { findNavController() }
    private val args: ESenseDeviceFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = EsenseDeviceFragmentBinding.inflate(inflater, container, false).apply {
            vm = viewModel
            lifecycleOwner = this@ESenseDeviceFragment

            sampleRateSlider.setup()
            buttonEnabledSwitch.setOnCheckedChangeListener { _, isChecked -> viewModel.setButtonPressEnabled(isChecked) }
            setupAccelerometerSettings()
            setupGyroSensorSettings()
            esenseDeviceSaveFab.setOnClickListener {
                val config = viewModel.device.value?.config
                val result = config?.let {
                    (activity as? MainActivity)?.earableService?.setConfig(args.device, it)
                } ?: false
                val snackbarMessage = when {
                    result -> getString(R.string.config_saved_successful)
                    else -> getString(R.string.config_saved_failed)
                }
                Snackbar.make(root, snackbarMessage, Snackbar.LENGTH_SHORT).show()
            }
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun disconnectDevice() {
        (activity as? MainActivity)?.earableService?.disconnect(args.device)
        navController.popBackStack()
    }

    private fun Slider.setup() {
        setLabelFormatter { getString(R.string.sample_rate_label_formatter, it.toInt()) }
        addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) = Unit
            override fun onStopTrackingTouch(slider: Slider) {
                viewModel.setSampleRate(slider.value)
            }
        })
    }

    private fun EsenseDeviceFragmentBinding.setupAccelerometerSettings() {
        accEnabledSwitch.setOnCheckedChangeListener { _, isChecked -> viewModel.setAccelerometerEnabled(isChecked) }
        accRangeGroup.setOnCheckedChangeListener { _, checkedId ->
            viewModel.setAccelerometerRange(checkedId.toAccRange)
        }
        accLowPassBandwidthGroup.setOnCheckedChangeListener { _, checkedId ->
            val bandwidth = checkedId.toAccLPF
            bandwidth?.let { viewModel.setAccelerometerLPFBandwidth(it) }
        }
    }

    private val Int.toAccLPF: ESenseConfig.AccLPF?
        get() = when (this) {
            R.id.acc_low_pass_bandwidth_5 -> ESenseConfig.AccLPF.BW_5
            R.id.acc_low_pass_bandwidth_10 -> ESenseConfig.AccLPF.BW_10
            R.id.acc_low_pass_bandwidth_20 -> ESenseConfig.AccLPF.BW_20
            R.id.acc_low_pass_bandwidth_41 -> ESenseConfig.AccLPF.BW_41
            R.id.acc_low_pass_bandwidth_92 -> ESenseConfig.AccLPF.BW_92
            R.id.acc_low_pass_bandwidth_184 -> ESenseConfig.AccLPF.BW_184
            R.id.acc_low_pass_bandwidth_460 -> ESenseConfig.AccLPF.BW_460
            else -> ESenseConfig.AccLPF.DISABLED
        }

    private val Int.toAccRange: ESenseConfig.AccRange
        get() = when (this) {
            R.id.acc_range_2 -> ESenseConfig.AccRange.G_2
            R.id.acc_range_8 -> ESenseConfig.AccRange.G_8
            R.id.acc_range_16 -> ESenseConfig.AccRange.G_16
            else -> ESenseConfig.AccRange.G_4
        }

    private fun EsenseDeviceFragmentBinding.setupGyroSensorSettings() {
        gyroEnabledSwitch.setOnCheckedChangeListener { _, isChecked -> viewModel.setGyroSensorEnabled(isChecked) }
        gyroRangeGroup.setOnCheckedChangeListener { _, checkedId ->
            viewModel.setGyroSensorRange(checkedId.toGyroRange)
        }
        gyroLowPassBandwidthGroup.setOnCheckedChangeListener { _, checkedId ->
            checkedId.toGyroLPF?.let { viewModel.setGyroSensorLPFBandwidth(it) }
        }
    }

    private val Int.toGyroLPF: ESenseConfig.GyroLPF?
        get() = when (this) {
            R.id.gyro_low_pass_bandwidth_5 -> ESenseConfig.GyroLPF.BW_5
            R.id.gyro_low_pass_bandwidth_10 -> ESenseConfig.GyroLPF.BW_10
            R.id.gyro_low_pass_bandwidth_20 -> ESenseConfig.GyroLPF.BW_20
            R.id.gyro_low_pass_bandwidth_41 -> ESenseConfig.GyroLPF.BW_41
            R.id.gyro_low_pass_bandwidth_92 -> ESenseConfig.GyroLPF.BW_92
            R.id.gyro_low_pass_bandwidth_184 -> ESenseConfig.GyroLPF.BW_184
            R.id.gyro_low_pass_bandwidth_250 -> ESenseConfig.GyroLPF.BW_250
            R.id.gyro_low_pass_bandwidth_3600 -> ESenseConfig.GyroLPF.BW_3600
            else -> ESenseConfig.GyroLPF.DISABLED
        }

    private val Int.toGyroRange: ESenseConfig.GyroRange
        get() = when (this) {
            R.id.gyro_range_250 -> ESenseConfig.GyroRange.DEG_250
            R.id.gyro_range_1000 -> ESenseConfig.GyroRange.DEG_1000
            R.id.gyro_range_2000 -> ESenseConfig.GyroRange.DEG_2000
            else -> ESenseConfig.GyroRange.DEG_500
        }

    companion object {
        private val TAG = ESenseDeviceFragment::class.java.simpleName
    }
}