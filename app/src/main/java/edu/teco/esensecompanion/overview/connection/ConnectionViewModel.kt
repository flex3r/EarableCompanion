package edu.teco.esensecompanion.overview.connection

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ConnectionViewModel @ViewModelInject constructor(@Assisted savedStateHandle: SavedStateHandle) : ViewModel() {

    init {
        startSearch()
    }

    private val _devices = MutableLiveData(emptyList<ConnectionItem>())
    val devices: LiveData<List<ConnectionItem>>
        get() = _devices

    private val _connectionEvent = MutableLiveData<ConnectionEvent>(ConnectionEvent.Empty)
    val connectionEvent: LiveData<ConnectionEvent>
        get() = _connectionEvent

    val isConnecting: LiveData<Boolean> = _connectionEvent.map { it is ConnectionEvent.Connecting || it is ConnectionEvent.Pairing }

    fun connect(item: ConnectionItem) {
        connectAndDismiss(item)
    }

    private fun startSearch() = viewModelScope.launch {
        delay(1_000)
        _devices.value = listOf(DATA[0])
        delay(2_000)
        _devices.value = DATA
    }

    private fun connectAndDismiss(item: ConnectionItem) = viewModelScope.launch {
        _connectionEvent.value = ConnectionEvent.Connecting(item)

        delay(2_000)
        _connectionEvent.value = ConnectionEvent.Pairing(item)

        delay(2_000)
        _connectionEvent.value = ConnectionEvent.Connected(item)
    }

    companion object {
        private val DATA = listOf(
            ConnectionItem("eSense-1234", "72db"),
            ConnectionItem("eSense-4321", "85db")
        )
    }
}