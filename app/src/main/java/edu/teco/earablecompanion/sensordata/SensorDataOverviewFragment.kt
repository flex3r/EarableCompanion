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
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class SensorDataOverviewFragment : Fragment() {

    private val viewModel: SensorDataOverviewViewModel by viewModels()
    private val navController: NavController by lazy { findNavController() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val adapter = SensorDataOverviewAdapter(::onRemove) {
            val date = it.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val action = SensorDataOverviewFragmentDirections.actionSensorDataOverviewFragmentToSensorDataDetailFragment(it.title, it.id, date)
            navController.navigate(action)
        }
        viewModel.sensorDataItems.observe(viewLifecycleOwner, adapter::submitList)

        val binding = SensorDataOverviewFragmentBinding.inflate(inflater, container, false).apply {
            vm = viewModel
            lifecycleOwner = this@SensorDataOverviewFragment
            sensorDataRecyclerview.adapter = adapter
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

    companion object {
        private val TAG = SensorDataOverviewFragment::class.java.simpleName
    }
}