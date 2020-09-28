package edu.teco.esensecompanion.settings

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.teco.esensecompanion.data.SensorDataRepository
import kotlinx.coroutines.launch

class SettingsViewModel @ViewModelInject constructor(private val sensorDataRepository: SensorDataRepository, @Assisted savedStateHandle: SavedStateHandle) : ViewModel() {

    fun clearData() = viewModelScope.launch {
        sensorDataRepository.clearData()
    }
}