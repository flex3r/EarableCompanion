package edu.teco.earablecompanion.overview.values

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.test
import edu.teco.earablecompanion.data.SensorDataRecording
import edu.teco.earablecompanion.data.SensorDataRepository
import edu.teco.earablecompanion.data.entities.SensorData
import edu.teco.earablecompanion.data.entities.SensorDataEntry
import edu.teco.earablecompanion.utils.MainCoroutineScopeRule
import edu.teco.earablecompanion.utils.MockData
import edu.teco.earablecompanion.utils.mockkLog
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

class ValuesViewModelTest {

    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    @get:Rule
    val testRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ValuesViewModel
    private lateinit var sensorDataRepository: SensorDataRepository

    private val activeRecordingFlow = MutableStateFlow<SensorDataRecording?>(null)

    @Before
    fun init() {
        mockkLog()

        sensorDataRepository = mockk(relaxed = true)

        every { sensorDataRepository.activeRecording } returns activeRecordingFlow

        viewModel = ValuesViewModel(sensorDataRepository)
    }

    @Test
    fun testNoRecording() {
        viewModel.valuesItem.test().assertNoValue()
    }

    @Test
    fun testRecording() {
        val address = "address"
        val name = "name"
        val title = "asd"
        val mockDevice = MockData.mockDevice(address, name)
        val dateTime = LocalDateTime.now()
        val values = mutableMapOf(address to SensorDataEntry(timestamp = dateTime, accX = 42.0))
        val recording = SensorDataRecording(SensorData(title = title, createdAt = dateTime), listOf(mockDevice), values)
        activeRecordingFlow.value = recording

        val expected = ValuesItem(title, dateTime, values)
        viewModel.valuesItem.test().assertValue(expected)
    }
}