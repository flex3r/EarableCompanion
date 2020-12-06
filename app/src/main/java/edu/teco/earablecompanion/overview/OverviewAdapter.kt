package edu.teco.earablecompanion.overview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.teco.earablecompanion.databinding.*

class OverviewAdapter(
    private val onStartConnect: () -> Unit,
    private val onDisconnect: (OverviewItem.Device) -> Unit,
    private val onShowValues: () -> Unit,
    private val onCalibrate: (OverviewItem.Device) -> Unit,
    private val onMicEnabledClick: (enabled: Boolean) -> Unit,
    private val onDeviceClick: (item: OverviewItem.Device) -> Unit,
) : ListAdapter<OverviewItem, RecyclerView.ViewHolder>(DetectDiff()) {

    class DeviceViewHolder(val binding: OverviewDeviceItemBinding) : RecyclerView.ViewHolder(binding.root)
    class NoDevicesViewHolder(binding: OverviewNoDevicesItemBinding) : RecyclerView.ViewHolder(binding.root)
    class RecordingViewHolder(val binding: OverviewRecordItemBinding) : RecyclerView.ViewHolder(binding.root)
    class MicDisabledViewHolder(val binding: OverviewMicDisabledItemBinding) : RecyclerView.ViewHolder(binding.root)
    class MicEnabled(val binding: OverviewMicEnabledItemBinding) : RecyclerView.ViewHolder(binding.root)
    class AddDevice(val binding: OverviewAddDeviceItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        ITEM_VIEW_TYPE_DEVICE -> DeviceViewHolder(OverviewDeviceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        ITEM_VIEW_TYPE_NO_DEVICES -> NoDevicesViewHolder(OverviewNoDevicesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        ITEM_VIEW_TYPE_RECORDING -> RecordingViewHolder(OverviewRecordItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        ITEM_VIEW_TYPE_ENABLE_MIC -> MicDisabledViewHolder(OverviewMicDisabledItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        ITEM_VIEW_TYPE_DISABLE_MIC -> MicEnabled(OverviewMicEnabledItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        ITEM_VIEW_TYPE_ADD_DEVICE -> AddDevice(OverviewAddDeviceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        else -> throw ClassCastException("Unknown viewType $viewType")
    }

    override fun getItemViewType(position: Int): Int = when (currentList[position]) {
        is OverviewItem.Device -> ITEM_VIEW_TYPE_DEVICE
        is OverviewItem.NoDevices -> ITEM_VIEW_TYPE_NO_DEVICES
        is OverviewItem.Recording -> ITEM_VIEW_TYPE_RECORDING
        is OverviewItem.MicDisabled -> ITEM_VIEW_TYPE_ENABLE_MIC
        is OverviewItem.MicEnabled -> ITEM_VIEW_TYPE_DISABLE_MIC
        is OverviewItem.AddDevice -> ITEM_VIEW_TYPE_ADD_DEVICE
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DeviceViewHolder -> with(holder.binding) {
                val entry = getItem(position) as OverviewItem.Device
                device = entry
                root.setOnClickListener { onDeviceClick(entry) }
                buttonDisconnect.setOnClickListener { onDisconnect(entry) }
                buttonCalibrate.setOnClickListener { onCalibrate(entry) }
            }
            is RecordingViewHolder -> with(holder.binding) {
                recording = getItem(position) as OverviewItem.Recording
                buttonShowValues.setOnClickListener { onShowValues() }
            }
            is MicDisabledViewHolder -> with(holder.binding) {
                item = getItem(position) as OverviewItem.MicDisabled
                buttonEnableMic.setOnClickListener { onMicEnabledClick(true) }
            }
            is MicEnabled -> with(holder.binding) {
                item = getItem(position) as OverviewItem.MicEnabled
                buttonDisableMic.setOnClickListener { onMicEnabledClick(false) }
            }
            is AddDevice -> with(holder.binding) {
                item = getItem(position) as OverviewItem.AddDevice
                buttonConnect.setOnClickListener { onStartConnect() }
            }
        }
    }

    private class DetectDiff : DiffUtil.ItemCallback<OverviewItem>() {
        override fun areItemsTheSame(oldItem: OverviewItem, newItem: OverviewItem): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: OverviewItem, newItem: OverviewItem): Boolean = oldItem == newItem
    }

    companion object {
        private const val ITEM_VIEW_TYPE_DEVICE = 0
        private const val ITEM_VIEW_TYPE_NO_DEVICES = 1
        private const val ITEM_VIEW_TYPE_RECORDING = 3
        private const val ITEM_VIEW_TYPE_ENABLE_MIC = 4
        private const val ITEM_VIEW_TYPE_DISABLE_MIC = 5
        private const val ITEM_VIEW_TYPE_ADD_DEVICE = 6
    }
}