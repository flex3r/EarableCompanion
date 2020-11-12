package edu.teco.earablecompanion.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.test
import edu.teco.earablecompanion.data.SensorDataRecording
import edu.teco.earablecompanion.data.SensorDataRepository
import edu.teco.earablecompanion.utils.MainCoroutineScopeRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {
    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    @get:Rule
    val testRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SettingsViewModel
    private lateinit var sensorDataRepository: SensorDataRepository

    private val activeRecordingFlow = MutableStateFlow<SensorDataRecording?>(null)

    @Before
    fun init() {
        sensorDataRepository = mockk(relaxed = true)

        every { sensorDataRepository.activeRecording } returns activeRecordingFlow

        viewModel = SettingsViewModel(sensorDataRepository)
    }

    @Test
    fun testRecordingActive() {
        val observer = viewModel.recordingActive.test()
        observer.assertValue(false)

        activeRecordingFlow.value = mockk()
        observer.assertValue(true)
    }

    @Test
    fun testClearData() = coroutineScope.runBlockingTest {
        viewModel.clearData()
        coVerify { sensorDataRepository.removeAll() }
    }
}