package edu.teco.earablecompanion.sensordata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import edu.teco.earablecompanion.R
import edu.teco.earablecompanion.databinding.SensorDataOverviewFragmentBinding
import edu.teco.earablecompanion.utils.CreateZipDocumentContract
import edu.teco.earablecompanion.utils.extensions.observe
import edu.teco.earablecompanion.utils.extensions.setFabScrollBehavior
import edu.teco.earablecompanion.utils.extensions.showShortSnackbar
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class SensorDataOverviewFragment : Fragment() {

    private val viewModel: SensorDataOverviewViewModel by viewModels()
    private val navController: NavController by lazy { findNavController() }
    private lateinit var binding: SensorDataOverviewFragmentBinding

    private val createZipRegistration = registerForActivityResult(CreateZipDocumentContract()) { result ->
        val tempDir = requireContext().getExternalFilesDir(null)
        when {
            result == null || tempDir == null -> binding.root.showShortSnackbar(getString(R.string.export_file_creation_error))
            else -> requireContext().contentResolver.openOutputStream(result)?.let {
                viewModel.exportAllData(it, tempDir)
            } ?: binding.root.showShortSnackbar(getString(R.string.export_file_creation_error))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val adapter = SensorDataOverviewAdapter(::onRemove) {
            val date = it.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val action = SensorDataOverviewFragmentDirections.actionSensorDataOverviewFragmentToSensorDataDetailFragment(it.title, it.id, date)
            navController.navigate(action)
        }

        with(viewModel) {
            sensorDataItems.observe(viewLifecycleOwner, adapter::submitList)
            exportEventFlow.observe(viewLifecycleOwner, ::handleExportEvent)
        }

        binding = SensorDataOverviewFragmentBinding.inflate(inflater, container, false).apply {
            vm = viewModel
            lifecycleOwner = this@SensorDataOverviewFragment
            recyclerData.adapter = adapter
            recyclerData.setFabScrollBehavior(fabExportAll)
            fabExportAll.setOnClickListener { createZipRegistration.launch("Data.zip") }
        }

        return binding.root
    }

    private fun onRemove(data: SensorDataOverviewItem.Data) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.remove_data_dialog_title))
            .setPositiveButton(getString(R.string.remove)) { _, _ -> viewModel.removeData(data) }
            .setNegativeButton(getString(R.string.cancel)) { d, _ -> d.dismiss() }
            .show()
    }

    private fun handleExportEvent(event: SensorDataExportEvent) {
        when (event) {
            is SensorDataExportEvent.Finished -> binding.root.showShortSnackbar(getString(R.string.export_file_finished))
            is SensorDataExportEvent.Failed -> binding.root.showShortSnackbar(getString(R.string.export_file_export_error, event.cause))
            else -> Unit
        }
    }

    companion object {
        private val TAG = SensorDataOverviewFragment::class.java.simpleName
    }
}