package edu.teco.earablecompanion.sensordata

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.test
import edu.teco.earablecompanion.data.SensorDataRepository
import edu.teco.earablecompanion.data.entities.SensorData
import edu.teco.earablecompanion.utils.MainCoroutineScopeRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime

class SensorDataOverviewViewModelTest {
    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    @get:Rule
    val testRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SensorDataOverviewViewModel
    private lateinit var sensorDataRepository: SensorDataRepository

    private val sensorDataFlow = MutableStateFlow<List<SensorData>>(emptyList())

    @Before
    fun init() {
        sensorDataRepository = mockk(relaxed = true)

        every { sensorDataRepository.getSensorDataFlow() } returns sensorDataFlow

        viewModel = SensorDataOverviewViewModel(sensorDataRepository)
    }

    @Test
    fun testNoSensorData() {
        viewModel.sensorDataItems.test().assertValue(listOf(SensorDataOverviewItem.NoData))
    }

    @Test
    fun testSensorData() = coroutineScope.runBlockingTest {
        val dataId = 42L
        val title = "title"
        val created = LocalDateTime.now()
        val stopped = LocalDateTime.now()
        val duration = Duration.between(created, stopped)
        val entryCount = 33
        val data = SensorData(dataId, title, created, stopped)

        coEvery { sensorDataRepository.getDataEntryCount(dataId) } returns entryCount
        sensorDataFlow.value = listOf(data)

        val expected = listOf(
            SensorDataOverviewItem.Data(dataId, title, created, stopped, duration, entryCount)
        )
        viewModel.sensorDataItems.test().assertValue(expected)
    }

    @Test
    fun testRemoveData() = coroutineScope.runBlockingTest {
        val dataId = 42L
        val data = SensorDataOverviewItem.Data(dataId, "", LocalDateTime.now(), LocalDateTime.now(), Duration.ZERO, 1)

        viewModel.removeData(data)
        coVerify { sensorDataRepository.removeData(dataId) }
    }

    @Test
    fun testHasData() = coroutineScope.runBlockingTest {
        val observer = viewModel.hasData.test()
        observer.assertValue(false)

        val data = SensorData(42L, "title", LocalDateTime.now(), LocalDateTime.now())
        coEvery { sensorDataRepository.getDataEntryCount(42L) } returns 1
        sensorDataFlow.value = listOf(data)

        observer.assertValue(true)
    }
}