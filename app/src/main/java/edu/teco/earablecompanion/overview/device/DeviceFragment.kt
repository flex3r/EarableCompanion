package edu.teco.earablecompanion.overview.device

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import dagger.hilt.android.AndroidEntryPoint
import edu.teco.earablecompanion.MainActivity
import edu.teco.earablecompanion.R
import edu.teco.earablecompanion.databinding.DeviceFragmentBinding

@AndroidEntryPoint
class DeviceFragment : Fragment() {

    private val viewModel: DeviceViewModel by viewModels()
    private val navController: NavController by lazy { findNavController() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DeviceFragmentBinding.inflate(inflater, container, false).apply {
            vm = viewModel
            lifecycleOwner = this@DeviceFragment

            sampleRateSlider.setup()
            setupAccelerometerSettings()
            setupGyroSensorSettings()

        }

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) = inflater.inflate(R.menu.device_menu, menu)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.remove_device -> MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.remove_device_dialog_title))
                .setPositiveButton(getString(R.string.remove_device_dialog_positive)) { _, _ -> navController.popBackStack() }
                .setNegativeButton(getString(R.string.remove_device_dialog_negative)) { d, _ -> d.dismiss() }
                .show()
            else -> return false
        }
        return true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as? MainActivity)?.bottomNavigationVisible = false
    }

    override fun onDetach() {
        (activity as? MainActivity)?.bottomNavigationVisible = true
        super.onDetach()
    }

    private fun Slider.setup() {
        setLabelFormatter { getString(R.string.sample_rate_label_formatter, it) }
        addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) = Unit
            override fun onStopTrackingTouch(slider: Slider) {
                viewModel.setSampleRate(slider.value)
            }
        })
    }

    private fun DeviceFragmentBinding.setupAccelerometerSettings() {
        accEnabledSwitch.setOnCheckedChangeListener { _, isChecked -> viewModel.setAccelerometerEnabled(isChecked) }
        accLowPassSwitch.setOnCheckedChangeListener { _, isChecked -> viewModel.setAccelerometerLPFEnabled(isChecked) }
        accRangeGroup.setOnCheckedChangeListener { _, checkedId ->
            val range = when (checkedId) {
                R.id.acc_range_2 -> 2
                R.id.acc_range_8 -> 8
                R.id.acc_range_16 -> 16
                else -> 4 // default
            }
            viewModel.setAccelerometerRange(range)
        }
        accLowPassBandwidthGroup.setOnCheckedChangeListener { _, checkedId ->
            val bandwidth = when (checkedId) {
                R.id.acc_low_pass_bandwidth_10 -> 10
                R.id.acc_low_pass_bandwidth_20 -> 20
                R.id.acc_low_pass_bandwidth_41 -> 41
                R.id.acc_low_pass_bandwidth_92 -> 92
                R.id.acc_low_pass_bandwidth_184 -> 184
                R.id.acc_low_pass_bandwidth_460 -> 460
                else -> 5
            }
            viewModel.setAccelerometerLPFBandwidth(bandwidth)
        }
    }

    private fun DeviceFragmentBinding.setupGyroSensorSettings() {
        gyroEnabledSwitch.setOnCheckedChangeListener { _, isChecked -> viewModel.setGyroSensorEnabled(isChecked) }
        gyroLowPassSwitch.setOnCheckedChangeListener { _, isChecked -> viewModel.setGyroSensorLPFEnabled(isChecked) }
        gyroRangeGroup.setOnCheckedChangeListener { _, checkedId ->
            val range = when (checkedId) {
                R.id.gyro_range_250 -> 250
                R.id.gyro_range_1000 -> 1000
                R.id.gyro_range_2000 -> 2000
                else -> 500
            }
            viewModel.setAccelerometerRange(range)
        }
        accLowPassBandwidthGroup.setOnCheckedChangeListener { _, checkedId ->
            val bandwidth = when (checkedId) {
                R.id.gyro_low_pass_bandwidth_10 -> 10
                R.id.gyro_low_pass_bandwidth_20 -> 20
                R.id.gyro_low_pass_bandwidth_41 -> 41
                R.id.gyro_low_pass_bandwidth_92 -> 92
                R.id.gyro_low_pass_bandwidth_184 -> 184
                R.id.gyro_low_pass_bandwidth_250 -> 250
                R.id.gyro_low_pass_bandwidth_3600 -> 3600
                else -> 5 // default
            }
            viewModel.setAccelerometerLPFBandwidth(bandwidth)
        }
    }

    companion object {
        private val TAG = DeviceFragment::class.java.simpleName
    }
}