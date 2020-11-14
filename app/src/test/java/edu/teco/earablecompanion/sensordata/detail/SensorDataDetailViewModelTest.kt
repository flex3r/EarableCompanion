package edu.teco.earablecompanion.sensordata.detail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.github.mikephil.charting.data.Entry
import com.jraska.livedata.test
import edu.teco.earablecompanion.data.SensorDataRepository
import edu.teco.earablecompanion.data.entities.SensorData
import edu.teco.earablecompanion.utils.MainCoroutineScopeRule
import edu.teco.earablecompanion.utils.MockData.sensorDataEntriesAndItems
import edu.teco.earablecompanion.utils.mockkLog
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
import kotlin.test.assertEquals

@Suppress("BlockingMethodInNonBlockingContext")
class SensorDataDetailViewModelTest {
    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    @get:Rule
    val testRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SensorDataDetailViewModel
    private lateinit var sensorDataRepository: SensorDataRepository
    private lateinit var savedStateHandle: SavedStateHandle

    private val sensorDataFlow = MutableStateFlow<SensorData?>(null)
    private val dataId: Long = 42L

    @Before
    fun init() {
        mockkLog()

        sensorDataRepository = mockk(relaxed = true)
        savedStateHandle = mockk(relaxed = true)

        every { sensorDataRepository.getSensorDataByIdFlow(dataId) } returns sensorDataFlow
        every { savedStateHandle.get<Long>("dataId") } returns dataId

        viewModel = SensorDataDetailViewModel(sensorDataRepository, savedStateHandle)
    }

    @Test
    fun testDetailDescription() {
        val title = "title"
        val created = LocalDateTime.now()
        val stopped = LocalDateTime.now()
        val duration = Duration.between(created, stopped)
        val entryCount = 33
        val data = SensorData(dataId, title, created, stopped)

        coEvery { sensorDataRepository.getDataEntryCount(dataId) } returns entryCount
        sensorDataFlow.value = data

        val expected = SensorDataDetailDescription(title, created, stopped, duration, entryCount, false)
        viewModel.detailDescription.test().assertValue(expected)
    }

    @Test
    fun testDetailDescriptionWithMic() {
        val title = "title"
        val created = LocalDateTime.now()
        val stopped = LocalDateTime.now()
        val duration = Duration.between(created, stopped)
        val entryCount = 33
        val data = SensorData(dataId, title, created, stopped, "/path/to/recording")

        coEvery { sensorDataRepository.getDataEntryCount(dataId) } returns entryCount
        sensorDataFlow.value = data

        val expected = SensorDataDetailDescription(title, created, stopped, duration, entryCount, true)
        viewModel.detailDescription.test().assertValue(expected)
    }

    @Test
    fun testNoDetailData() = coroutineScope.runBlockingTest {
        coEvery { sensorDataRepository.getSensorDataEntries(dataId) } returns emptyList()

        viewModel.detailData.test().assertValueHistory(
            listOf(SensorDataDetailItem.Loading),
            listOf(SensorDataDetailItem.NoData)
        )
    }

    @Test
    fun testDetailData() = coroutineScope.runBlockingTest {
        val name = "name"
        val address = "address"
        val (data, expected) = sensorDataEntriesAndItems(name, address)
        coEvery { sensorDataRepository.getSensorDataEntries(dataId) } returns data

        val observer = viewModel.detailData.test()
        observer.awaitNextValue()
        observer.assertValue { compareItems(expected, it) }
        assertEquals(listOf(SensorDataDetailItem.Loading), observer.valueHistory().first())
    }

    @Test
    fun testIsNotActive() {
        coEvery { sensorDataRepository.getDataEntryCount(dataId) } returns 33
        sensorDataFlow.value = SensorData(dataId, "title", LocalDateTime.now(), null, "/path/to/recording")

        val observer = viewModel.isNotActive.test()
        observer.assertValue(false)

        sensorDataFlow.value = SensorData(dataId, "title", LocalDateTime.now(), LocalDateTime.now(), "/path/to/recording")
        observer.assertValue(true)
    }

    @Test
    fun testHasData() {
        coEvery { sensorDataRepository.getDataEntryCount(dataId) } returns 0
        sensorDataFlow.value = SensorData(dataId, "title", LocalDateTime.now(), null, "/path/to/recording")

        val observer = viewModel.hasData.test()
        observer.assertValue(false)

        coEvery { sensorDataRepository.getDataEntryCount(dataId) } returns 1
        sensorDataFlow.value = SensorData(dataId, "asd", LocalDateTime.now(), LocalDateTime.now(), "/path/to/recording")
        observer.assertValue(true)
    }

    @Test
    fun testHasMic() {
        coEvery { sensorDataRepository.getDataEntryCount(dataId) } returns 1
        sensorDataFlow.value = SensorData(dataId, "title", LocalDateTime.now(), null, null)

        val observer = viewModel.hasMic.test()
        observer.assertValue(false)

        sensorDataFlow.value = SensorData(dataId, "title", LocalDateTime.now(), LocalDateTime.now(), "/path/to/recording")
        observer.assertValue(true)
    }

    @Test
    fun testHasNoLogs() {
        coEvery { sensorDataRepository.hasLogs(dataId) } returns false
        viewModel.hasLogs.test().assertValue(false)
    }

    @Test
    fun testHasLogs() {
        val dataId = 42L
        coEvery { sensorDataRepository.hasLogs(dataId) } returns true
        viewModel.hasLogs.test().assertValue(true)
    }

    @Test
    fun testRemoveData() = coroutineScope.runBlockingTest {
        viewModel.removeData()
        coVerify { sensorDataRepository.removeData(dataId) }
    }

    @Test
    fun testEditData() = coroutineScope.runBlockingTest {
        val title = "title"
        viewModel.editData(title)
        coVerify { sensorDataRepository.updateSensorData(dataId, title) }
    }

    @Test
    fun testLoadLogs() = coroutineScope.runBlockingTest {
        viewModel.loadLogs()
        coVerify { sensorDataRepository.getLogEntries(dataId) }
    }

    private fun compareItems(expected: List<SensorDataDetailItem>, actual: List<SensorDataDetailItem>): Boolean {
        if (expected.size != actual.size) return false

        for (i in expected.indices) {
            val expectedItem = expected[i] as? SensorDataDetailItem.Chart ?: return false
            val actualItem = actual[i] as? SensorDataDetailItem.Chart ?: return false

            if (expectedItem.deviceName != actualItem.deviceName
                || expectedItem.deviceAddress != actualItem.deviceAddress
                || expectedItem.type != actualItem.type
                || !compareEntries(expectedItem.data, actualItem.data)
            ) return false
        }

        return true
    }

    private fun compareEntries(expected: List<Entry>, actual: List<Entry>): Boolean {
        if (expected.size != actual.size) return false

        for (i in expected.indices) {
            if (!expected[i].equalTo(actual[i])) return false
        }

        return true
    }
}