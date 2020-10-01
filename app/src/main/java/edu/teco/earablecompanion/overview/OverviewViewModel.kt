package edu.teco.earablecompanion.overview

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class OverviewViewModel @ViewModelInject constructor(@Assisted savedStateHandle: SavedStateHandle) : ViewModel() {

    private val _devices = MutableLiveData(DATA)
    val devices: LiveData<List<OverviewItem>>
        get() = _devices

    private val _connectionOpen = MutableLiveData(false)
    val connectionOpen: LiveData<Boolean>
        get() = _connectionOpen

    fun setConnectionOpen(open: Boolean) {
        _connectionOpen.value = open
    }

    companion object {
        private val DATA = listOf(
            OverviewItem.NoDevices,
            OverviewItem.Device(123, "eSense-1234", "85db", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."),
            OverviewItem.Device(124, "eSense-4321", "72db", "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."),
        )
    }
}