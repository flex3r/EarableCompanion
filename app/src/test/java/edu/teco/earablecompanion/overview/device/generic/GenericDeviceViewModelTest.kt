package edu.teco.earablecompanion.overview.device.generic

import android.bluetooth.BluetoothDevice
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.jraska.livedata.test
import edu.teco.earablecompanion.bluetooth.ConnectionRepository
import edu.teco.earablecompanion.bluetooth.config.Config
import edu.teco.earablecompanion.bluetooth.config.CosinussConfig
import edu.teco.earablecompanion.bluetooth.config.ESenseConfig
import edu.teco.earablecompanion.bluetooth.config.GenericConfig
import edu.teco.earablecompanion.overview.device.esense.ESenseDeviceItem
import edu.teco.earablecompanion.overview.device.esense.ESenseDeviceViewModel
import edu.teco.earablecompanion.utils.MainCoroutineScopeRule
import edu.teco.earablecompanion.utils.MockData
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GenericDeviceViewModelTest {
    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    @get:Rule
    val testRule = InstantTaskExecutorRule()

    private lateinit var viewModel: GenericDeviceViewModel
    private lateinit var connectionRepository: ConnectionRepository
    private lateinit var savedStateHandle: SavedStateHandle

    private val device = MockData.mockDevice()
    private val deviceConfigsFlow = MutableStateFlow<Map<String, Config>>(mutableMapOf())

    @Before
    fun init() {
        connectionRepository = mockk(relaxed = true)
        savedStateHandle = mockk(relaxed = true)

        every { savedStateHandle.get<BluetoothDevice>("device") } returns device
        every { connectionRepository.deviceConfigs } returns deviceConfigsFlow

        viewModel = GenericDeviceViewModel(connectionRepository, savedStateHandle)
    }

    @Test
    fun testDeviceDefaultConfig() {
        val expected = GenericDeviceItem(device.name, GenericConfig())
        viewModel.device.test().assertValue(expected)
    }

    @Test
    fun testDeviceCustomConfig() {
        val config = GenericConfig(heartRateSupported = true, oximeterSupported = true)
        deviceConfigsFlow.value = mutableMapOf(device.address to config)

        val expected = GenericDeviceItem(device.name, config)
        viewModel.device.test().assertValue(expected)
    }

    @Test
    fun testHeartRateSupported() {
        val observer = viewModel.heartRateSupported.test()
        observer.assertValue(false)

        val config = GenericConfig(heartRateSupported = true)
        deviceConfigsFlow.value = mutableMapOf(device.address to config)
        observer.assertValue(true)
    }

    @Test
    fun testBodyTemperatureSupported() {
        val observer = viewModel.bodyTemperatureSupported.test()
        observer.assertValue(false)

        val config = GenericConfig(bodyTemperatureSupported = true)
        deviceConfigsFlow.value = mutableMapOf(device.address to config)
        observer.assertValue(true)
    }
}