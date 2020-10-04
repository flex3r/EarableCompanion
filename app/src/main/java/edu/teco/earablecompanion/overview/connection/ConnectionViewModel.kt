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
    connectionRepository: ConnectionRepository,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel() {

    val devices: LiveData<List<ConnectionItem>> = liveData(viewModelScope.coroutineContext) {
        connectionRepository.scanResult.collectLatest { scanResult ->
            emit(scanResult.map { it.value }.toConnectionItems())
        }
    }

    private val _connectionEvent = MutableLiveData<ConnectionEvent>(ConnectionEvent.Empty)
    val connectionEvent: LiveData<ConnectionEvent>
        get() = _connectionEvent

    val isConnecting: LiveData<Boolean> = _connectionEvent.map { it is ConnectionEvent.Connecting || it is ConnectionEvent.Pairing }

    fun connect(item: ConnectionItem) {
        connectAndDismiss(item)
    }

    private fun connectAndDismiss(item: ConnectionItem) = viewModelScope.launch {
        _connectionEvent.value = ConnectionEvent.Connecting(item)

        delay(2_000)
        _connectionEvent.value = ConnectionEvent.Pairing(item)

        delay(2_000)
        _connectionEvent.value = ConnectionEvent.Connected(item)
    }
}