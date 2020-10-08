package edu.teco.earablecompanion.overview

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import edu.teco.earablecompanion.bluetooth.ConnectionRepository
import edu.teco.earablecompanion.overview.OverviewItem.Device.Companion.toOverviewItems
import kotlinx.coroutines.flow.collectLatest

class OverviewViewModel @ViewModelInject constructor(
    connectionRepository: ConnectionRepository,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel() {

    val devices: LiveData<List<OverviewItem>> = liveData(viewModelScope.coroutineContext) {
        connectionRepository.connectedDevices.collectLatest { devices ->
            val items = devices.values.toOverviewItems()
            when {
                items.isEmpty() -> emit(listOf(OverviewItem.NoDevices))
                else -> emit(items)
            }
        }
    }

    val hasConnectedDevices = devices.map { items -> items.any { it is OverviewItem.Device } }

    private val _connectionOpen = MutableLiveData(false)
    val connectionOpen: LiveData<Boolean>
        get() = _connectionOpen

    fun setConnectionOpen(open: Boolean) {
        _connectionOpen.value = open
    }
}