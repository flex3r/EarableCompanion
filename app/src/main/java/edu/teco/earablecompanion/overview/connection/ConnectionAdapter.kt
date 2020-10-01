package edu.teco.earablecompanion.overview.connection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.teco.earablecompanion.databinding.ConnectionDeviceItemBinding

class ConnectionAdapter(private val onClick: (ConnectionItem) -> Unit) : ListAdapter<ConnectionItem, ConnectionAdapter.ConnectionItemViewHolder>(DetectDiff()) {
    var clickEnabled = true

    class ConnectionItemViewHolder(val binding: ConnectionDeviceItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConnectionItemViewHolder {
        return ConnectionItemViewHolder(ConnectionDeviceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ConnectionItemViewHolder, position: Int) {
        val entry = getItem(position)
        holder.binding.apply {
            item = entry
            root.isClickable = clickEnabled
            root.isFocusable = clickEnabled
            if (clickEnabled)
                root.setOnClickListener { onClick(entry) }
        }
    }

    private class DetectDiff : DiffUtil.ItemCallback<ConnectionItem>() {
        override fun areContentsTheSame(oldItem: ConnectionItem, newItem: ConnectionItem): Boolean = oldItem == newItem
        override fun areItemsTheSame(oldItem: ConnectionItem, newItem: ConnectionItem): Boolean = oldItem == newItem
    }
}