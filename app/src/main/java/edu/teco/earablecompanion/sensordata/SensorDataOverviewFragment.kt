package edu.teco.earablecompanion.sensordata

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import edu.teco.earablecompanion.databinding.SensorDataOverviewFragmentBinding

@AndroidEntryPoint
class SensorDataOverviewFragment : Fragment() {

    private val viewModel: SensorDataOverviewViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val adapter = SensorDataOverviewAdapter {
            Log.d(TAG, "onClick $it")
        }
        viewModel.sensorDataItems.observe(viewLifecycleOwner) { adapter.submitList(it) }

        val binding = SensorDataOverviewFragmentBinding.inflate(inflater, container, false).apply {
            vm = viewModel
            lifecycleOwner = this@SensorDataOverviewFragment
            sensorDataRecyclerview.adapter = adapter
        }

        return binding.root
    }

    companion object {
        private val TAG = SensorDataOverviewFragment::class.java.simpleName
    }
}