package edu.teco.earablecompanion.sensordata.detail

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import edu.teco.earablecompanion.MainActivity
import edu.teco.earablecompanion.R
import edu.teco.earablecompanion.databinding.SensorDataDetailFragmentBinding

@AndroidEntryPoint
class SensorDataDetailFragment : Fragment() {
    private val viewModel: SensorDataDetailViewModel by viewModels()
    private val navController: NavController by lazy { findNavController() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val adapter = SensorDataDetailAdapter(::editDescription)

        viewModel.detailItems.observe(viewLifecycleOwner) { adapter.submitList(it) }

        val binding = SensorDataDetailFragmentBinding.inflate(inflater, container, false).apply {
            vm = viewModel
            lifecycleOwner = this@SensorDataDetailFragment
            sensorDataDetailRecyclerview.adapter = adapter
            exportFab.setOnClickListener {
                // TODO
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
                .setPositiveButton(getString(R.string.remove_data_dialog_positive)) { _, _ -> removeData() }
                .setNegativeButton(getString(R.string.remove_data_dialog_negative)) { d, _ -> d.dismiss() }
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

    private fun editDescription() {
        // TODO
    }

    companion object {
        private val TAG = SensorDataDetailFragment::class.java.simpleName
    }
}