package edu.teco.earablecompanion.overview.calibration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import edu.teco.earablecompanion.databinding.CalibrationFragmentBinding
import edu.teco.earablecompanion.overview.connection.ConnectionEvent

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

        viewModel.apply {
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val sheetContainer = requireView().parent as? ViewGroup ?: return
        sheetContainer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
    }

    companion object {
        private val TAG = CalibrationFragment::class.java.simpleName
    }
}