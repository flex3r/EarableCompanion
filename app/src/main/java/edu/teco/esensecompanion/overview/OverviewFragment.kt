package edu.teco.esensecompanion.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import edu.teco.esensecompanion.databinding.OverviewFragmentBinding
import edu.teco.esensecompanion.overview.connection.ConnectionFragment

@AndroidEntryPoint
class OverviewFragment : Fragment() {

    private val viewModel: OverviewViewModel by viewModels()
    private val navController: NavController by lazy { findNavController() }
    private lateinit var binding: OverviewFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val adapter = OverviewAdapter {
            val action = OverviewFragmentDirections.actionOverviewFragmentToDeviceFragment(it.name)
            navController.navigate(action)
        }
        viewModel.devices.observe(viewLifecycleOwner) { adapter.submitList(it) }

        binding = OverviewFragmentBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@OverviewFragment
            vm = viewModel
            devicesRecyclerview.adapter = adapter
            connectFab.setOnClickListener(::showConnectionBottomSheet)
        }

        return binding.root
    }

    fun onCancelConnectionBottomSheet() {
        viewModel.setConnectionOpen(false)
    }

    fun showSnackbar(text: String) = Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()

    private fun showConnectionBottomSheet(view: View?) {
        viewModel.setConnectionOpen(true)
        val dialog = ConnectionFragment()
        dialog.show(childFragmentManager, ConnectionFragment::class.java.simpleName)
    }

    companion object {
        private val TAG = OverviewFragment::class.java.simpleName
    }
}