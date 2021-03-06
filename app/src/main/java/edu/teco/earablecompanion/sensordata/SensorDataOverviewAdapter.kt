package edu.teco.earablecompanion.sensordata

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.teco.earablecompanion.databinding.SensorDataItemBinding
import edu.teco.earablecompanion.databinding.SensorDataLoadingItemBinding
import edu.teco.earablecompanion.databinding.SensorDataNoDataItemBinding

class SensorDataOverviewAdapter(
    private val onRemove: (SensorDataOverviewItem.Data) -> Unit,
    private val onClick: (SensorDataOverviewItem.Data) -> Unit,
) : ListAdapter<SensorDataOverviewItem, RecyclerView.ViewHolder>(DetectDiff()) {

    class DataViewHolder(val binding: SensorDataItemBinding) : RecyclerView.ViewHolder(binding.root)
    class NoDataViewHolder(binding: SensorDataNoDataItemBinding) : RecyclerView.ViewHolder(binding.root)
    class LoadingViewHolder(binding: SensorDataLoadingItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        ITEM_VIEW_TYPE_DATA -> DataViewHolder(SensorDataItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        ITEM_VIEW_TYPE_NO_DATA -> NoDataViewHolder(SensorDataNoDataItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        ITEM_VIEW_TYPE_LOADING -> LoadingViewHolder(SensorDataLoadingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        else -> throw ClassCastException("Unknown viewType $viewType")
    }

    override fun getItemViewType(position: Int): Int = when (currentList[position]) {
        is SensorDataOverviewItem.Data -> ITEM_VIEW_TYPE_DATA
        is SensorDataOverviewItem.NoData -> ITEM_VIEW_TYPE_NO_DATA
        is SensorDataOverviewItem.Loading -> ITEM_VIEW_TYPE_LOADING
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DataViewHolder -> {
                val entry = getItem(position) as SensorDataOverviewItem.Data
                with(holder.binding) {
                    data = entry
                    root.setOnClickListener { onClick(entry) }
                    buttonRemove.setOnClickListener { onRemove(entry) }
                }
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
        private const val ITEM_VIEW_TYPE_LOADING = 2
    }
}