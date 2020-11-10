package edu.teco.earablecompanion.overview.device.cosinuss

import android.bluetooth.BluetoothDevice
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.jraska.livedata.test
import edu.teco.earablecompanion.bluetooth.ConnectionRepository
import edu.teco.earablecompanion.bluetooth.config.Config
import edu.teco.earablecompanion.bluetooth.config.CosinussConfig
import edu.teco.earablecompanion.utils.MainCoroutineScopeRule
import edu.teco.earablecompanion.utils.MockData.mockDevice
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CosinussDeviceViewModelTest {
    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    @get:Rule
    val testRule = InstantTaskExecutorRule()

    private lateinit var viewModel: CosinussDeviceViewModel
    private lateinit var connectionRepository: ConnectionRepository
    private lateinit var savedStateHandle: SavedStateHandle

    private val device = mockDevice()
    private val deviceConfigsFlow = MutableStateFlow<Map<String, Config>>(mutableMapOf())

    @Before
    fun init() {
        connectionRepository = mockk(relaxed = true)
        savedStateHandle = mockk(relaxed = true)

        every { savedStateHandle.get<BluetoothDevice>("device") } returns device
        every { connectionRepository.deviceConfigs } returns deviceConfigsFlow

        viewModel = CosinussDeviceViewModel(connectionRepository, savedStateHandle)
    }

    @Test
    fun testDeviceDefaultConfig() {
        val expected = CosinussDeviceItem(device.name, CosinussConfig())
        viewModel.device.test().assertValue(expected)
    }

    @Test
    fun testDeviceCustomConfig() {
        val config = CosinussConfig(accSupported = true, accEnabled = true)
        deviceConfigsFlow.value = mutableMapOf(device.address to config)

        val expected = CosinussDeviceItem(device.name, config)
        viewModel.device.test().assertValue(expected)
    }

    @Test
    fun testAccSupported() {
        val observer = viewModel.accSupported.test()
        observer.assertValue(false)

        val config = CosinussConfig(accSupported = true)
        deviceConfigsFlow.value = mutableMapOf(device.address to config)
        observer.assertValue(true)
    }
}