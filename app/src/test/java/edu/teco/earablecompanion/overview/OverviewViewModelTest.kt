package edu.teco.earablecompanion.overview

import android.bluetooth.BluetoothDevice
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.test
import edu.teco.earablecompanion.bluetooth.ConnectionRepository
import edu.teco.earablecompanion.bluetooth.EarableType
import edu.teco.earablecompanion.bluetooth.config.Config
import edu.teco.earablecompanion.data.SensorDataRecording
import edu.teco.earablecompanion.data.SensorDataRepository
import edu.teco.earablecompanion.data.entities.SensorData
import edu.teco.earablecompanion.utils.MainCoroutineScopeRule
import edu.teco.earablecompanion.utils.MockData.mockConfig
import edu.teco.earablecompanion.utils.MockData.mockDevice
import edu.teco.earablecompanion.utils.mockkLog
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OverviewViewModelTest {
    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    @get:Rule
    val testRule = InstantTaskExecutorRule()

    private lateinit var viewModel: OverviewViewModel
    private lateinit var connectionRepository: ConnectionRepository
    private lateinit var sensorDataRepository: SensorDataRepository

    private val connectedDevicesFlow = MutableStateFlow<MutableMap<String, BluetoothDevice>>(mutableMapOf())
    private val deviceConfigsFlow = MutableStateFlow<Map<String, Config>>(mutableMapOf())
    private val bluetoothScoActiveFlow = MutableStateFlow<Boolean?>(null)
    private val micEnabledFlow = MutableStateFlow(false)
    private val activeRecordingFlow = MutableStateFlow<SensorDataRecording?>(null)

    @Before
    fun init() {
        mockkLog()

        connectionRepository = mockk(relaxed = true)
        sensorDataRepository = mockk(relaxed = true)

        every { connectionRepository.connectedDevices } returns connectedDevicesFlow
        every { connectionRepository.deviceConfigs } returns deviceConfigsFlow
        every { connectionRepository.bluetoothScoActive } returns bluetoothScoActiveFlow
        every { connectionRepository.micEnabled } returns micEnabledFlow
        every { sensorDataRepository.activeRecording } returns activeRecordingFlow

        viewModel = OverviewViewModel(connectionRepository, sensorDataRepository)
    }

    @Test
    fun testNoDevices() {
        viewModel.overviewItems.test().assertValue(listOf(OverviewItem.NoDevices))
    }

    @Test
    fun testDevicesNoConfigNotRecording() {
        val address = "address"
        val name = "name"
        val mockDevice = mockDevice(address, name)
        connectedDevicesFlow.value = mutableMapOf(address to mockDevice)

        val expected = listOf(
            OverviewItem.Device(name, address, mockDevice, EarableType.NotSupported, false)
        )
        viewModel.overviewItems.test().assertValue(expected)
    }

    @Test
    fun testMultipleDevices() {
        val address1 = "address1"
        val address2 = "address2"
        val name1 = "name1"
        val name2 = "name2"
        val device1 = mockDevice(address1, name1)
        val device2 = mockDevice(address2, name2)
        connectedDevicesFlow.value = mutableMapOf(address1 to device1, address2 to device2)

        val expected = listOf(
            OverviewItem.Device(name1, address1, device1, EarableType.NotSupported, false),
            OverviewItem.Device(name2, address2, device2, EarableType.NotSupported, false)
        )
        viewModel.overviewItems.test().assertValue(expected)
    }

    @Test
    fun testDevicesWithConfigNotRecording() {
        val address = "address"
        val name = "name"
        val mockDevice = mockDevice(address, name)
        val mockConfig = mockConfig(EarableType.Generic(heartRateSupported = true, oximeterSupported = true))
        deviceConfigsFlow.value = mutableMapOf(address to mockConfig)
        connectedDevicesFlow.value = mutableMapOf(address to mockDevice)

        val expected = listOf(
            OverviewItem.Device(name, address, mockDevice, EarableType.Generic(heartRateSupported = true, oximeterSupported = true), false)
        )
        viewModel.overviewItems.test().assertValue(expected)
    }

    @Test
    fun testDevicesWithConfigAndRecording() {
        val address = "address"
        val name = "name"
        val mockDevice = mockDevice(address, name)
        val mockConfig = mockConfig(EarableType.Generic())
        val dateTime = LocalDateTime.now()
        val recording = SensorDataRecording(SensorData(title = "asd", createdAt = dateTime), listOf(mockDevice))
        deviceConfigsFlow.value = mutableMapOf(address to mockConfig)
        connectedDevicesFlow.value = mutableMapOf(address to mockDevice)
        activeRecordingFlow.value = recording

        val expected = listOf(
            OverviewItem.Recording(dateTime, listOf(mockDevice)),
            OverviewItem.Device(name, address, mockDevice, EarableType.Generic(), false)
        )
        viewModel.overviewItems.test().assertValue(expected)
    }

    @Test
    fun testDevicesWithConfigAndCalibrationSupported() {
        val address = "address"
        val name = "name"
        val mockDevice = mockDevice(address, name)
        val mockConfig = mockConfig(EarableType.Generic(), calibrate = true)
        deviceConfigsFlow.value = mutableMapOf(address to mockConfig)
        connectedDevicesFlow.value = mutableMapOf(address to mockDevice)

        val expected = listOf(
            OverviewItem.Device(name, address, mockDevice, EarableType.Generic(), true)
        )
        viewModel.overviewItems.test().assertValue(expected)
    }


    @Test
    fun testBondedDevicesMicEnabledNoSCO() {
        val address = "address"
        val name = "name"
        val mockDevice = mockDevice(address, name)
        connectedDevicesFlow.value = mutableMapOf(address to mockDevice)
        micEnabledFlow.value = true
        bluetoothScoActiveFlow.value = false

        val expected = listOf(
            OverviewItem.MicEnabled(scoConnected = false, recordingActive = false),
            OverviewItem.Device(name, address, mockDevice, EarableType.NotSupported, false),
        )
        viewModel.overviewItems.test().assertValue(expected)
    }

    @Test
    fun testBondedDevicesMicEnabledAndSco() {
        val address = "address"
        val name = "name"
        val mockDevice = mockDevice(address, name)
        connectedDevicesFlow.value = mutableMapOf(address to mockDevice)
        micEnabledFlow.value = true
        bluetoothScoActiveFlow.value = true

        val expected = listOf(
            OverviewItem.MicEnabled(scoConnected = true, recordingActive = false),
            OverviewItem.Device(name, address, mockDevice, EarableType.NotSupported, false),
        )
        viewModel.overviewItems.test().assertValue(expected)
    }

    @Test
    fun testBondedDevicesMicEnabledAndScoAndRecording() {
        val address = "address"
        val name = "name"
        val mockDevice = mockDevice(address, name)
        val dateTime = LocalDateTime.now()
        val recording = SensorDataRecording(SensorData(title = "asd", createdAt = dateTime), listOf(mockDevice))
        connectedDevicesFlow.value = mutableMapOf(address to mockDevice)
        micEnabledFlow.value = true
        bluetoothScoActiveFlow.value = true
        activeRecordingFlow.value = recording

        val expected = listOf(
            OverviewItem.Recording(dateTime, listOf(mockDevice)),
            OverviewItem.MicEnabled(scoConnected = true, recordingActive = true),
            OverviewItem.Device(name, address, mockDevice, EarableType.NotSupported, false),
        )
        viewModel.overviewItems.test().assertValue(expected)
    }

    @Test
    fun testBondedDevicesMicDisabled() {
        val address = "address"
        val name = "name"
        val mockDevice = mockDevice(address, name)
        connectedDevicesFlow.value = mutableMapOf(address to mockDevice)
        micEnabledFlow.value = false
        bluetoothScoActiveFlow.value = true

        val expected = listOf(
            OverviewItem.MicDisabled(false),
            OverviewItem.Device(name, address, mockDevice, EarableType.NotSupported, false),
        )
        viewModel.overviewItems.test().assertValue(expected)
    }

    @Test
    fun testBondedDevicesMicDisabledRecording() {
        val address = "address"
        val name = "name"
        val mockDevice = mockDevice(address, name)
        val dateTime = LocalDateTime.now()
        val recording = SensorDataRecording(SensorData(title = "asd", createdAt = dateTime), listOf(mockDevice))
        connectedDevicesFlow.value = mutableMapOf(address to mockDevice)
        micEnabledFlow.value = false
        bluetoothScoActiveFlow.value = true
        activeRecordingFlow.value = recording

        val expected = listOf(
            OverviewItem.Recording(dateTime, listOf(mockDevice)),
            OverviewItem.MicDisabled(true),
            OverviewItem.Device(name, address, mockDevice, EarableType.NotSupported, false),
        )
        viewModel.overviewItems.test().assertValue(expected)
    }

    @Test
    fun testNoBondedDevicesMicEnabled() {
        val address = "address"
        val name = "name"
        val mockDevice = mockDevice(address, name, BluetoothDevice.BOND_NONE)
        connectedDevicesFlow.value = mutableMapOf(address to mockDevice)
        micEnabledFlow.value = true
        bluetoothScoActiveFlow.value = true

        val expected = listOf(
            OverviewItem.Device(name, address, mockDevice, EarableType.NotSupported, false),
        )
        viewModel.overviewItems.test().assertValue(expected)
    }

    @Test
    fun testConnectedDevicesAndRecordingInitial() {
        viewModel.connectedDevicesAndRecording.test().assertValue(false to false)
    }

    @Test
    fun testUnSupportedConnectedDevicesAndNoRecording() {
        val address = "address"
        val name = "name"
        val mockDevice = mockDevice(address, name)
        connectedDevicesFlow.value = mutableMapOf(address to mockDevice)

        viewModel.connectedDevicesAndRecording.test().assertValue(false to false)
    }

    @Test
    fun testConnectedDevicesAndNoRecording() {
        val address = "address"
        val name = "name"
        val mockDevice = mockDevice(address, name)
        val mockConfig = mockConfig(EarableType.Generic())
        deviceConfigsFlow.value = mutableMapOf(address to mockConfig)
        connectedDevicesFlow.value = mutableMapOf(address to mockDevice)

        viewModel.connectedDevicesAndRecording.test().assertValue(true to false)
    }

    @Test
    fun testConnectedDevicesAndRecording() {
        val address = "address"
        val name = "name"
        val mockDevice = mockDevice(address, name)
        val mockConfig = mockConfig(EarableType.Generic())
        val dateTime = LocalDateTime.now()
        val recording = SensorDataRecording(SensorData(title = "asd", createdAt = dateTime), listOf(mockDevice))
        deviceConfigsFlow.value = mutableMapOf(address to mockConfig)
        connectedDevicesFlow.value = mutableMapOf(address to mockDevice)
        activeRecordingFlow.value = recording

        viewModel.connectedDevicesAndRecording.test().assertValue(true to true)
    }

    @Test
    fun testMicRecordingPossible() {
        assertFalse(viewModel.micRecordingPossible)

        bluetoothScoActiveFlow.value = true
        assertFalse(viewModel.micRecordingPossible)

        micEnabledFlow.value = true
        assertTrue(viewModel.micRecordingPossible)
    }
}