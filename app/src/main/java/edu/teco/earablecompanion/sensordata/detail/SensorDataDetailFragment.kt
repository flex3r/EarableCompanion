package edu.teco.earablecompanion.sensordata.detail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import edu.teco.earablecompanion.databinding.SensorDataDetailFragmentBinding

@AndroidEntryPoint
class SensorDataDetailFragment : Fragment() {
    private val viewModel: SensorDataDetailViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel.dataItem.observe(viewLifecycleOwner) {
            Log.d(TAG, it.toString())
        }

        val binding = SensorDataDetailFragmentBinding.inflate(inflater, container, false).apply {
            vm = viewModel
            lifecycleOwner = this@SensorDataDetailFragment
        }

        return binding.root
    }

    companion object {
        private val TAG = SensorDataDetailFragment::class.java.simpleName
    }
}