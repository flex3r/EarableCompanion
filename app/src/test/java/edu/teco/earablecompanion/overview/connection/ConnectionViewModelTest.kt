package edu.teco.earablecompanion.overview.connection

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.test
import edu.teco.earablecompanion.bluetooth.ConnectionRepository
import edu.teco.earablecompanion.utils.MainCoroutineScopeRule
import edu.teco.earablecompanion.utils.MockData.mockConfig
import edu.teco.earablecompanion.utils.MockData.mockDevice
import edu.teco.earablecompanion.utils.MockData.mockScanResult
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import no.nordicsemi.android.support.v18.scanner.ScanResult
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ConnectionViewModelTest {
    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    @get:Rule
    val testRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ConnectionViewModel
    private lateinit var connectionRepository: ConnectionRepository

    private val scanResultFlow = MutableStateFlow<Map<String, ScanResult>>(mutableMapOf())
    private val connectionEventFlow = MutableStateFlow<ConnectionEvent>(ConnectionEvent.Empty)

    @Before
    fun init() {
        connectionRepository = mockk(relaxed = true)

        every { connectionRepository.scanResult } returns scanResultFlow
        every { connectionRepository.connectionEvent } returns connectionEventFlow

        viewModel = ConnectionViewModel(connectionRepository)
    }

    @Test
    fun testEmptyResult() {
        viewModel.devices.test().assertValue(listOf())
    }

    @Test
    fun testSingleScanResult() {
        val name = "name"
        val address = "address"
        val rssi = -42
        val device = mockDevice(address, name)
        val scanResult = mockScanResult(true, rssi, device)
        scanResultFlow.value = mutableMapOf(address to scanResult)

        val expected = listOf(
            ConnectionItem(name, address, "$rssi db", device)
        )
        viewModel.devices.test().assertValue(expected)
    }

    @Test
    fun testMultipleScanResults() {
        val address1 = "address1"
        val address2 = "address2"
        val name1 = "name1"
        val name2 = "name2"
        val rssi1 = -42
        val rssi2 = 42
        val device1 = mockDevice(address1, name1)
        val device2 = mockDevice(address2, name2)
        val scanResult1 = mockScanResult(true, rssi1, device1)
        val scanResult2 = mockScanResult(true, rssi2, device2)
        scanResultFlow.value = mutableMapOf(address1 to scanResult1, address2 to scanResult2)

        val expected = listOf(
            ConnectionItem(name1, address1, "$rssi1 db", device1),
            ConnectionItem(name2, address2, "$rssi2 db", device2)
        )
        viewModel.devices.test().assertValue(expected)
    }

    @Test
    fun testConnectionEvent() {
        val observer = viewModel.connectionEvent.test()
        observer.assertValue(ConnectionEvent.Empty)

        val device = mockDevice()
        connectionEventFlow.value = ConnectionEvent.Connecting(device)
        observer.assertValue(ConnectionEvent.Connecting(device))

        val config = mockConfig()
        connectionEventFlow.value = ConnectionEvent.Connected(device, config)
        observer.assertValue(ConnectionEvent.Connected(device, config))
    }

    @Test
    fun testIsConnecting() {
        val observer = viewModel.isConnecting.test()
        observer.assertValue(false)

        val device = mockDevice()
        connectionEventFlow.value = ConnectionEvent.Connecting(device)
        observer.assertValue(true)

        val config = mockConfig()
        connectionEventFlow.value = ConnectionEvent.Connected(device, config)
        observer.assertValue(false)
    }
}