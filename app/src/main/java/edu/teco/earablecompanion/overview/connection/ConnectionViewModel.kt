package edu.teco.earablecompanion.overview.connection

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import edu.teco.earablecompanion.bluetooth.ConnectionRepository
import edu.teco.earablecompanion.overview.connection.ConnectionItem.Companion.toConnectionItems
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ConnectionViewModel @ViewModelInject constructor(
    private val connectionRepository: ConnectionRepository,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel() {

    val devices: LiveData<List<ConnectionItem>> = liveData(viewModelScope.coroutineContext) {
        connectionRepository.scanResult.collectLatest { scanResult ->
            emit(scanResult.values.toConnectionItems())
        }
    }

    val connectionEvent: LiveData<ConnectionEvent> = connectionRepository.connectionEvent.asLiveData(viewModelScope.coroutineContext)
    val isConnecting: LiveData<Boolean> = connectionEvent.map { it is ConnectionEvent.Connecting }

    fun clearConnectionEvent() = connectionRepository.clearConnectionEvent()
}