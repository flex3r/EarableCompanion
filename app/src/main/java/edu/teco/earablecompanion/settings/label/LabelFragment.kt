package edu.teco.earablecompanion.settings.label

import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import edu.teco.earablecompanion.R
import edu.teco.earablecompanion.databinding.LabelFragmentBinding
import edu.teco.earablecompanion.utils.extensions.isLandscape

class LabelFragment : BottomSheetDialogFragment() {

    private lateinit var binding: LabelFragmentBinding
    private lateinit var labelAdapter: LabelAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var key: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = LabelFragmentBinding.inflate(inflater, container, false).apply {
            iconClose.setOnClickListener { requireDialog().dismiss() }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(view.context)
        key = getString(R.string.preference_recording_labels_key)

        val defaultString = getString(R.string.preference_recording_labels_default)
        val defaults = defaultString.split(",").map { it.trim() }
        val labels: List<LabelItem> = sharedPreferences.getString(key, defaultString)
            ?.split(",")
            ?.map { LabelItem.Label(it.trim()) }
            ?: defaults.map { LabelItem.Label(it) }

        val entries = labels.toMutableList()
        entries.add(LabelItem.Add)

        labelAdapter = LabelAdapter(entries, defaults)
        binding.recyclerLabels.adapter = labelAdapter
    }

    override fun onStart() {
        super.onStart()
        (view?.parent as? ViewGroup)?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        (dialog as? BottomSheetDialog)?.behavior?.state = when {
            isLandscape -> BottomSheetBehavior.STATE_EXPANDED
            else -> BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        val labelString = labelAdapter.entries
            .filterIsInstance<LabelItem.Label>()
            .filter { it.name.isNotBlank() }
            .distinct()
            .joinToString { it.name.replace(",", "") }

        sharedPreferences.edit { putString(key, labelString) }

        super.onDismiss(dialog)
    }

    companion object {
        private val TAG = LabelFragment::class.java.simpleName
    }
}