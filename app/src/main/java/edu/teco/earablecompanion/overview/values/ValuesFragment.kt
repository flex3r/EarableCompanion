package edu.teco.earablecompanion.overview.values

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import edu.teco.earablecompanion.databinding.ValuesFragmentBinding
import edu.teco.earablecompanion.utils.extensions.isLandscape

@AndroidEntryPoint
class ValuesFragment : BottomSheetDialogFragment() {

    private val viewModel: ValuesViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = ValuesFragmentBinding.inflate(inflater, container, false).apply {
            vm = viewModel
            lifecycleOwner = this@ValuesFragment
            iconClose.setOnClickListener { requireDialog().cancel() }
        }

        return binding.root
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
        private val TAG = ValuesFragment::class.java.simpleName
    }
}