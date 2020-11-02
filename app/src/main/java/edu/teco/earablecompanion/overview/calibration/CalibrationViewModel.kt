package edu.teco.earablecompanion.overview.calibration

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import edu.teco.earablecompanion.bluetooth.ConnectionRepository

class CalibrationViewModel @ViewModelInject constructor(
    private val connectionRepository: ConnectionRepository,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel()