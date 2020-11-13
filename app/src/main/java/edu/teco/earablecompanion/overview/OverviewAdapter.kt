package edu.teco.earablecompanion.overview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.teco.earablecompanion.databinding.*

class OverviewAdapter(
    private val onDisconnect: (OverviewItem.Device) -> Unit,
    private val onCalibrate: (OverviewItem.Device) -> Unit,
    private val onMicEnabledClick: (enabled: Boolean) -> Unit,
    private val onDeviceClick: (item: OverviewItem.Device) -> Unit,
) :
    ListAdapter<OverviewItem, RecyclerView.ViewHolder>(DetectDiff()) {

    class DeviceViewHolder(val binding: OverviewDeviceItemBinding) : RecyclerView.ViewHolder(binding.root)
    class NoDevicesViewHolder(binding: OverviewNoDevicesItemBinding) : RecyclerView.ViewHolder(binding.root)
    class RecordingViewHolder(val binding: OverviewRecordItemBinding) : RecyclerView.ViewHolder(binding.root)
    class MicDisabledViewHolder(val binding: OverviewMicDisabledItemBinding) : RecyclerView.ViewHolder(binding.root)
    class MicEnabled(val binding: OverviewMicEnabledItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        ITEM_VIEW_TYPE_DEVICE -> DeviceViewHolder(OverviewDeviceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        ITEM_VIEW_TYPE_NO_DEVICES -> NoDevicesViewHolder(OverviewNoDevicesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        ITEM_VIEW_TYPE_RECORDING -> RecordingViewHolder(OverviewRecordItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        ITEM_VIEW_TYPE_ENABLE_MIC -> MicDisabledViewHolder(OverviewMicDisabledItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        ITEM_VIEW_TYPE_DISABLE_MIC -> MicEnabled(OverviewMicEnabledItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        else -> throw ClassCastException("Unknown viewType $viewType")
    }

    override fun getItemViewType(position: Int): Int = when (currentList[position]) {
        is OverviewItem.Device -> ITEM_VIEW_TYPE_DEVICE
        is OverviewItem.NoDevices -> ITEM_VIEW_TYPE_NO_DEVICES
        is OverviewItem.Recording -> ITEM_VIEW_TYPE_RECORDING
        is OverviewItem.MicDisabled -> ITEM_VIEW_TYPE_ENABLE_MIC
        is OverviewItem.MicEnabled -> ITEM_VIEW_TYPE_DISABLE_MIC
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DeviceViewHolder -> {
                val entry = getItem(position) as OverviewItem.Device
                with(holder.binding) {
                    device = entry
                    root.setOnClickListener { onDeviceClick(entry) }
                    overviewDeviceDisconnect.setOnClickListener { onDisconnect(entry) }
                    overviewDeviceCalibrate.setOnClickListener { onCalibrate(entry) }
                }
            }
            is RecordingViewHolder -> {
                holder.binding.recording = getItem(position) as OverviewItem.Recording
            }
            is MicDisabledViewHolder -> {
                holder.binding.item = getItem(position) as OverviewItem.MicDisabled
                holder.binding.overviewMicDisabledButton.setOnClickListener { onMicEnabledClick(true) }
            }
            is MicEnabled -> {
                holder.binding.item = getItem(position) as OverviewItem.MicEnabled
                holder.binding.overviewMicEnabledButton.setOnClickListener { onMicEnabledClick(false) }
            }
        }
    }

    private class DetectDiff : DiffUtil.ItemCallback<OverviewItem>() {
        override fun areItemsTheSame(oldItem: OverviewItem, newItem: OverviewItem): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: OverviewItem, newItem: OverviewItem): Boolean {
            return if (newItem is OverviewItem.Recording) false else oldItem == newItem
        }
    }

    companion object {
        private const val ITEM_VIEW_TYPE_DEVICE = 0
        private const val ITEM_VIEW_TYPE_NO_DEVICES = 1
        private const val ITEM_VIEW_TYPE_RECORDING = 3
        private const val ITEM_VIEW_TYPE_ENABLE_MIC = 4
        private const val ITEM_VIEW_TYPE_DISABLE_MIC = 5
    }
}