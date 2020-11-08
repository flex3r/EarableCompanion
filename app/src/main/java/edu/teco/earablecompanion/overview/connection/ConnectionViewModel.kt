package edu.teco.earablecompanion.overview.connection

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import edu.teco.earablecompanion.bluetooth.ConnectionRepository
import edu.teco.earablecompanion.overview.connection.ConnectionItem.Companion.toConnectionItems
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collectLatest

class ConnectionViewModel @ViewModelInject constructor(
    private val connectionRepository: ConnectionRepository,
) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, Log.getStackTraceString(throwable))
    }

    val devices: LiveData<List<ConnectionItem>> = liveData(viewModelScope.coroutineContext + coroutineExceptionHandler) {
        connectionRepository.scanResult.collectLatest { scanResult ->
            emit(scanResult.values.toConnectionItems())
        }
    }

    val connectionEvent: LiveData<ConnectionEvent> = connectionRepository.connectionEvent.asLiveData(viewModelScope.coroutineContext + coroutineExceptionHandler)
    val isConnecting: LiveData<Boolean> = connectionEvent.map { it is ConnectionEvent.Connecting }

    fun clearConnectionEvent() = connectionRepository.clearConnectionEvent()

    companion object {
        private val TAG = ConnectionViewModel::class.java.simpleName
    }
}