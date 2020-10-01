package edu.teco.earablecompanion.sensordata

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.teco.earablecompanion.databinding.NoSensorDataItemBinding
import edu.teco.earablecompanion.databinding.SensorDataItemBinding

class SensorDataOverviewAdapter(private val onClick: (SensorDataOverviewItem.Data) -> Unit) : ListAdapter<SensorDataOverviewItem, RecyclerView.ViewHolder>(DetectDiff()) {

    class DataViewHolder(val binding: SensorDataItemBinding) : RecyclerView.ViewHolder(binding.root)
    class NoDataViewHolder(binding: NoSensorDataItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        ITEM_VIEW_TYPE_DATA -> DataViewHolder(SensorDataItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        ITEM_VIEW_TYPE_NO_DATA -> NoDataViewHolder(NoSensorDataItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        else -> throw ClassCastException("Unknown viewType $viewType")
    }

    override fun getItemViewType(position: Int): Int = when (currentList[position]) {
        is SensorDataOverviewItem.Data -> ITEM_VIEW_TYPE_DATA
        is SensorDataOverviewItem.NoData -> ITEM_VIEW_TYPE_NO_DATA
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DataViewHolder -> {
                val entry = getItem(position) as SensorDataOverviewItem.Data
                holder.binding.data = entry
                holder.binding.root.setOnClickListener { onClick(entry) }
            }
        }
    }

    private class DetectDiff : DiffUtil.ItemCallback<SensorDataOverviewItem>() {
        override fun areContentsTheSame(oldItem: SensorDataOverviewItem, newItem: SensorDataOverviewItem): Boolean = oldItem == newItem
        override fun areItemsTheSame(oldItem: SensorDataOverviewItem, newItem: SensorDataOverviewItem): Boolean = oldItem == newItem
    }

    companion object {
        private const val ITEM_VIEW_TYPE_DATA = 0
        private const val ITEM_VIEW_TYPE_NO_DATA = 1
    }
}