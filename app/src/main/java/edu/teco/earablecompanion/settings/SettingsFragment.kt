package edu.teco.earablecompanion.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import edu.teco.earablecompanion.BuildConfig
import edu.teco.earablecompanion.R

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    private val navController: NavController by lazy { findNavController() }
    private val viewModel: SettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findPreference<Preference>(getString(R.string.preference_about_key))?.summary = getString(R.string.preference_about_summary, BuildConfig.VERSION_NAME)
        findPreference<Preference>(getString(R.string.preference_clear_data_key))?.setOnPreferenceClickListener {
            MaterialAlertDialogBuilder(view.context)
                .setTitle(getString(R.string.clear_data_dialog_title))
                .setPositiveButton(getString(R.string.clear_data_dialog_positive)) { _, _ -> viewModel.clearData() }
                .setNegativeButton(getString(R.string.clear_data_dialog_negative)) { d, _ -> d.dismiss() }
                .show()
            true
        }

        viewModel.recordingActive.observe(viewLifecycleOwner) {
            findPreference<SwitchPreferenceCompat>(getString(R.string.preference_record_microphone_key))?.isEnabled = !it
            findPreference<SwitchPreferenceCompat>(getString(R.string.preference_intercept_media_buttons_key))?.isEnabled = !it
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }
}