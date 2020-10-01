package edu.teco.earablecompanion.overview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.teco.earablecompanion.databinding.OverviewDeviceItemBinding
import edu.teco.earablecompanion.databinding.OverviewNoDevicesItemBinding

class OverviewAdapter(private val onClick: (item: OverviewItem.Device) -> Unit) : ListAdapter<OverviewItem, RecyclerView.ViewHolder>(DetectDiff()) {

    class DeviceViewHolder(val binding: OverviewDeviceItemBinding) : RecyclerView.ViewHolder(binding.root)
    class NoDevicesViewHolder(binding: OverviewNoDevicesItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        ITEM_VIEW_TYPE_DEVICE -> DeviceViewHolder(OverviewDeviceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        ITEM_VIEW_TYPE_NO_DEVICES -> NoDevicesViewHolder(OverviewNoDevicesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        else -> throw ClassCastException("Unknown viewType $viewType")
    }

    override fun getItemViewType(position: Int): Int = when (currentList[position]) {
        is OverviewItem.Device -> ITEM_VIEW_TYPE_DEVICE
        is OverviewItem.NoDevices -> ITEM_VIEW_TYPE_NO_DEVICES
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DeviceViewHolder -> {
                val entry = getItem(position) as OverviewItem.Device
                holder.binding.device = entry
                holder.binding.root.setOnClickListener { onClick(entry) }
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
    }
}