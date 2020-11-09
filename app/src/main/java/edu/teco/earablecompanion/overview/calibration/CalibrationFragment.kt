package edu.teco.earablecompanion.overview.calibration

import android.bluetooth.BluetoothDevice
import android.os.Bundle
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
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_COLLAPSED

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.getParcelable<BluetoothDevice>(DEVICE_ARG)?.let {
            (activity as? MainActivity)?.earableService?.startCalibration(it)
        }
    }

    override fun onStop() {
        (activity as? MainActivity)?.earableService?.stopCalibration()
        dialog?.cancel()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        val sheetContainer = requireView().parent as? ViewGroup ?: return
        sheetContainer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
    }

    companion object {
        private val TAG = CalibrationFragment::class.java.simpleName
        const val DEVICE_ARG = "device_arg"

        fun newInstance(device: BluetoothDevice) = CalibrationFragment().apply {
            arguments = bundleOf(DEVICE_ARG to device)
        }
    }
}