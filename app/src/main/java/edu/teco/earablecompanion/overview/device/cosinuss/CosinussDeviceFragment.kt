package edu.teco.earablecompanion.overview.device.cosinuss

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import edu.teco.earablecompanion.MainActivity
import edu.teco.earablecompanion.R
import edu.teco.earablecompanion.databinding.CosinussDeviceFragmentBinding

@AndroidEntryPoint
class CosinussDeviceFragment : Fragment() {

    private val viewModel: CosinussDeviceViewModel by viewModels()
    private val navController: NavController by lazy { findNavController() }
    private val args: CosinussDeviceFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = CosinussDeviceFragmentBinding.inflate(inflater, container, false).apply {
            vm = viewModel
            lifecycleOwner = this@CosinussDeviceFragment

            heartRateEnabledSwitch.setOnCheckedChangeListener { _, isChecked -> viewModel.setHeartRateEnabled(isChecked) }
            bodyTemperatureEnabledSwitch.setOnCheckedChangeListener { _, isChecked -> viewModel.setBodyTemperatureEnabled(isChecked) }
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) = inflater.inflate(R.menu.device_menu, menu)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.remove_device -> MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.remove_device_dialog_title))
                .setPositiveButton(getString(R.string.remove)) { _, _ -> disconnectDevice() }
                .setNegativeButton(getString(R.string.cancel)) { d, _ -> d.dismiss() }
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

    private fun disconnectDevice() {
        (activity as? MainActivity)?.earableService?.disconnect(args.device)
        navController.popBackStack()
    }
}