package edu.teco.earablecompanion.overview.calibration

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import edu.teco.earablecompanion.MainActivity
import edu.teco.earablecompanion.databinding.CalibrationFragmentBinding
import edu.teco.earablecompanion.utils.extensions.isLandscape

@AndroidEntryPoint
class CalibrationFragment : BottomSheetDialogFragment() {

    private val viewModel: CalibrationViewModel by viewModels()
    private lateinit var binding: CalibrationFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = CalibrationFragmentBinding.inflate(inflater, container, false).apply {
            vm = viewModel
            lifecycleOwner = this@CalibrationFragment
            calibrationCloseIcon.setOnClickListener { requireDialog().cancel() }
        }

        viewModel.calibrationActive.observe(viewLifecycleOwner) { active ->
            if (!active) {
                requireDialog().cancel()
            }
        }

        if (savedInstanceState == null) {
            arguments?.getParcelable<BluetoothDevice>(DEVICE_ARG)?.let {
                (activity as? MainActivity)?.earableService?.startCalibration(it)
            }
        }

        return binding.root
    }

    override fun onStop() {
        if (activity?.isChangingConfigurations == false) {
            (activity as? MainActivity)?.earableService?.stopCalibration()
            dialog?.cancel()
        }

        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        (view?.parent as? ViewGroup)?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        (dialog as? BottomSheetDialog)?.behavior?.state = when {
            isLandscape -> BottomSheetBehavior.STATE_EXPANDED
            else -> BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    companion object {
        private val TAG = CalibrationFragment::class.java.simpleName
        const val DEVICE_ARG = "device_arg"

        fun newInstance(device: BluetoothDevice) = CalibrationFragment().apply {
            arguments = bundleOf(DEVICE_ARG to device)
        }
    }
}