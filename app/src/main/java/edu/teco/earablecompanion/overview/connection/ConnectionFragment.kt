package edu.teco.earablecompanion.overview.connection

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import edu.teco.earablecompanion.MainActivity
import edu.teco.earablecompanion.R
import edu.teco.earablecompanion.databinding.ConnectionFragmentBinding
import edu.teco.earablecompanion.overview.OverviewFragment
import edu.teco.earablecompanion.utils.extensions.isLandscape
import edu.teco.earablecompanion.utils.extensions.valueOrFalse
import kotlinx.coroutines.delay

@AndroidEntryPoint
class ConnectionFragment : BottomSheetDialogFragment() {

    private val viewModel: ConnectionViewModel by viewModels()
    private lateinit var binding: ConnectionFragmentBinding
    private lateinit var connectionAdapter: ConnectionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ConnectionFragmentBinding.inflate(inflater, container, false).apply {
            vm = viewModel
            lifecycleOwner = this@ConnectionFragment
            connectionCloseIcon.setOnClickListener { requireDialog().cancel() }
        }

        viewModel.apply {
            devices.observe(viewLifecycleOwner) { connectionAdapter.submitList(it) }
            connectionEvent.observe(viewLifecycleOwner, ::handleConnectionEvent)
            isConnecting.observe(viewLifecycleOwner) {
                binding.root.post {
                    connectionAdapter.clickEnabled = !it
                    binding.connectionDevicesRecyclerview.swapAdapter(connectionAdapter, false)
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val linearLayoutManager = LinearLayoutManager(view.context)
        connectionAdapter = ConnectionAdapter(::startConnect)

        binding.connectionDevicesRecyclerview.apply {
            layoutManager = linearLayoutManager
            adapter = connectionAdapter.apply {
                registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        super.onItemRangeInserted(positionStart, itemCount)
                        if (positionStart == 0 && positionStart == linearLayoutManager.findFirstCompletelyVisibleItemPosition()) {
                            // auto-scroll up if recyclerview was at top before new item was added
                            linearLayoutManager.scrollToPosition(0)
                        }
                    }
                })
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (view?.parent as? ViewGroup)?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        (dialog as? BottomSheetDialog)?.behavior?.state = when {
            isLandscape -> BottomSheetBehavior.STATE_EXPANDED
            else -> BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as? MainActivity)?.requestPermissions()
    }

    override fun onCancel(dialog: DialogInterface) {
        (activity as? MainActivity)?.earableService?.stopScan()
        viewModel.clearConnectionEvent()
    }

    private fun startConnect(item: ConnectionItem) {
        if (viewModel.isConnecting.valueOrFalse) return
        (activity as? MainActivity)?.earableService?.connectOrBond(item.device)
    }

    private fun handleConnectionEvent(event: ConnectionEvent) {
        when (event) {
            is ConnectionEvent.Pairing -> binding.connectionHeaderText.text = formatDeviceName(R.string.connection_header_text_pairing, event.device.name)
            is ConnectionEvent.Connecting -> binding.connectionHeaderText.text = formatDeviceName(R.string.connection_header_text_connecting, event.device.name)
            is ConnectionEvent.Connected -> {
                cancelDialog(delay = 1_000)
                (parentFragment as? OverviewFragment)?.onConnected(event)

            }
            is ConnectionEvent.Failed -> {
                cancelDialog()
                (parentFragment as? OverviewFragment)?.onConnectionFailed()
            }
            else -> binding.connectionHeaderText.text = getString(R.string.connection_header_text)
        }
    }

    private fun formatDeviceName(@StringRes stringFormatRes: Int, name: String?): String {
        return getString(stringFormatRes, name ?: getString(R.string.unknown_device_name))
    }

    private fun cancelDialog(delay: Long = 0L) = lifecycleScope.launchWhenResumed {
        delay(delay)
        requireDialog().cancel()
        viewModel.clearConnectionEvent()
    }

    companion object {
        private val TAG = ConnectionFragment::class.java.simpleName
    }
}