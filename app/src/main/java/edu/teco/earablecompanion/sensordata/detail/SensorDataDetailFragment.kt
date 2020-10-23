package edu.teco.earablecompanion.sensordata.detail

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import edu.teco.earablecompanion.MainActivity
import edu.teco.earablecompanion.R
import edu.teco.earablecompanion.databinding.SensorDataDetailFragmentBinding
import edu.teco.earablecompanion.utils.CreateCsvDocumentContract
import edu.teco.earablecompanion.utils.extensions.observe

@AndroidEntryPoint
class SensorDataDetailFragment : Fragment() {

    private val viewModel: SensorDataDetailViewModel by viewModels()
    private val navController: NavController by lazy { findNavController() }
    private val args: SensorDataDetailFragmentArgs by navArgs()
    private lateinit var binding: SensorDataDetailFragmentBinding

    private val createDocumentRegistration = registerForActivityResult(CreateCsvDocumentContract()) { result ->
        when (result) {
            null -> showSnackbar(getString(R.string.export_file_creation_error))
            else -> requireContext().contentResolver.openOutputStream(result)?.let {
                viewModel.exportData(it)
            } ?: showSnackbar(getString(R.string.export_file_creation_error))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val adapter = SensorDataDetailAdapter()

        viewModel.detailData.observe(viewLifecycleOwner) { adapter.submitList(it) }
        viewModel.exportEventFlow.observe(viewLifecycleOwner, ::handleExportEvent)

        binding = SensorDataDetailFragmentBinding.inflate(inflater, container, false).apply {
            vm = viewModel
            lifecycleOwner = this@SensorDataDetailFragment
            sensorDataDetailRecyclerview.adapter = adapter
            sensorDataDetailDescriptionText.movementMethod = ScrollingMovementMethod()
            sensorDataDetailDescriptionEdit.setOnClickListener { editData() }
            exportFab.setOnClickListener {
                createDocumentRegistration.launch("${args.dataTitle}.csv")
            }
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) = inflater.inflate(R.menu.data_menu, menu)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.remove_data -> MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.remove_data_dialog_title))
                .setPositiveButton(getString(R.string.remove)) { _, _ -> removeData() }
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

    private fun removeData() {
        viewModel.removeData()
        navController.popBackStack()
    }

    private fun editData() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val layout = LayoutInflater.from(builder.context).inflate(R.layout.dialog_multi_input_layout, null) as LinearLayout
        layout.findViewById<TextInputLayout>(R.id.dialog_title_input_layout).hint = getString(R.string.edit_title_input_hint)
        layout.findViewById<TextInputLayout>(R.id.dialog_body_input_layout).hint = getString(R.string.edit_description_input_hint)

        val descriptionEditText = layout.findViewById<TextInputEditText>(R.id.dialog_body_input_text).apply {
            inputType = inputType or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setText(viewModel.description)
        }
        val titleEditText = layout.findViewById<TextInputEditText>(R.id.dialog_title_input_text).apply {
            setText(viewModel.title)
        }

        builder
            .setTitle(R.string.edit_dialog_title)
            .setView(layout)
            .setPositiveButton(R.string.save) { _, _ ->
                val description = descriptionEditText.text?.toString()
                val title = titleEditText.text?.toString() ?: getString(R.string.start_recording_dialog_title_default)
                viewModel.editData(title, description)
                navController.currentDestination?.label = title
                (requireActivity() as AppCompatActivity).supportActionBar?.title = title
            }
            .setNegativeButton(R.string.cancel) { d, _ -> d.dismiss() }
            .show()
    }

    private fun handleExportEvent(event: SensorDataExportEvent) {
        when (event) {
            is SensorDataExportEvent.Finished -> showSnackbar(getString(R.string.export_file_finished))
            is SensorDataExportEvent.Failed -> showSnackbar(getString(R.string.export_file_export_error, event.cause))
            else -> Unit
        }
    }

    private fun showSnackbar(text: String) = Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()

    companion object {
        private val TAG = SensorDataDetailFragment::class.java.simpleName
    }
}