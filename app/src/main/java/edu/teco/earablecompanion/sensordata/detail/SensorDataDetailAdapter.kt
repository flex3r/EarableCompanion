package edu.teco.earablecompanion.sensordata.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import edu.teco.earablecompanion.R
import edu.teco.earablecompanion.databinding.SensorDataDetailChartItemBinding
import edu.teco.earablecompanion.databinding.SensorDataDetailLoadingItemBinding
import edu.teco.earablecompanion.databinding.SensorDataDetailNoDataItemBinding
import edu.teco.earablecompanion.utils.extensions.themeColor

class SensorDataDetailAdapter : ListAdapter<SensorDataDetailItem, RecyclerView.ViewHolder>(DetectDiff()) {

    class ChartViewHolder(val binding: SensorDataDetailChartItemBinding) : RecyclerView.ViewHolder(binding.root)
    class NoDataViewHolder(binding: SensorDataDetailNoDataItemBinding) : RecyclerView.ViewHolder(binding.root)
    class LoadingViewHolder(binding: SensorDataDetailLoadingItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        ITEM_VIEW_TYPE_CHART -> ChartViewHolder(SensorDataDetailChartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        ITEM_VIEW_TYPE_NO_DATA -> NoDataViewHolder(SensorDataDetailNoDataItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        ITEM_VIEW_TYPE_LOADING -> LoadingViewHolder(SensorDataDetailLoadingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        else -> throw ClassCastException("Unknown viewType $viewType")
    }

    override fun getItemViewType(position: Int): Int = when (currentList[position]) {
        is SensorDataDetailItem.Chart -> ITEM_VIEW_TYPE_CHART
        is SensorDataDetailItem.NoData -> ITEM_VIEW_TYPE_NO_DATA
        is SensorDataDetailItem.Loading -> ITEM_VIEW_TYPE_LOADING
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ChartViewHolder -> with(holder.binding) {
                sensorDataDetailChart.setup()
                item = getItem(position) as SensorDataDetailItem.Chart
            }
        }
    }

    private fun LineChart.setup() {
        description.isEnabled = false
        legend.isEnabled = false
        isDoubleTapToZoomEnabled = false
        isHighlightPerTapEnabled = false
        isHighlightPerDragEnabled = false
        isKeepPositionOnRotation = true
        setDrawBorders(true)
        setBorderColor(context.themeColor(R.attr.colorPrimary))
        xAxis.setDrawGridLines(false)
        xAxis.setDrawLabels(false)
        axisLeft.setDrawGridLines(false)
        axisLeft.textColor = context.themeColor(android.R.attr.textColorPrimary)
        axisRight.isEnabled = false
    }

    private class DetectDiff : DiffUtil.ItemCallback<SensorDataDetailItem>() {
        override fun areItemsTheSame(oldItem: SensorDataDetailItem, newItem: SensorDataDetailItem): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: SensorDataDetailItem, newItem: SensorDataDetailItem): Boolean = oldItem == newItem
    }

    companion object {
        private const val ITEM_VIEW_TYPE_CHART = 1
        private const val ITEM_VIEW_TYPE_NO_DATA = 2
        private const val ITEM_VIEW_TYPE_LOADING = 3
    }
}