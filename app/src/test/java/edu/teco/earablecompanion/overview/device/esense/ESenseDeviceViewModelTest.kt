package edu.teco.earablecompanion.overview.device.esense

import android.bluetooth.BluetoothDevice
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.jraska.livedata.test
import edu.teco.earablecompanion.bluetooth.ConnectionRepository
import edu.teco.earablecompanion.bluetooth.config.Config
import edu.teco.earablecompanion.bluetooth.config.ESenseConfig
import edu.teco.earablecompanion.utils.MainCoroutineScopeRule
import edu.teco.earablecompanion.utils.MockData
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ESenseDeviceViewModelTest {
    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    @get:Rule
    val testRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ESenseDeviceViewModel
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

        viewModel = ESenseDeviceViewModel(connectionRepository, savedStateHandle)
    }

    @Test
    fun testDeviceDefaultConfig() {
        val expected = ESenseDeviceItem(device.name, ESenseConfig())
        viewModel.device.test().assertValue(expected)
    }

    @Test
    fun testDeviceCustomConfig() {
        val config = ESenseConfig(accEnabled = false, buttonEnabled = false, accLPF = ESenseConfig.AccLPF.DISABLED)
        deviceConfigsFlow.value = mutableMapOf(device.address to config)

        val expected = ESenseDeviceItem(device.name, config)
        viewModel.device.test().assertValue(expected)
    }
}