package edu.teco.earablecompanion.overview.calibration

import android.bluetooth.BluetoothDevice
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.jraska.livedata.test
import edu.teco.earablecompanion.utils.MainCoroutineScopeRule
import edu.teco.earablecompanion.utils.MockData
import edu.teco.earablecompanion.utils.mockkLog
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CalibrationViewModelTest {

    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    @get:Rule
    val testRule = InstantTaskExecutorRule()

    private lateinit var viewModel: CalibrationViewModel
    private lateinit var savedStateHandle: SavedStateHandle

    private val device = MockData.mockDevice()

    @Before
    fun init() {
        mockkLog()
        savedStateHandle = mockk(relaxed = true)
        every { savedStateHandle.get<BluetoothDevice>("device_arg") } returns device

        viewModel = CalibrationViewModel(savedStateHandle)
    }

    @Test
    fun testState() = coroutineScope.runBlockingTest {
        var idx = 9
        val observer = viewModel.calibrationState.test()
        observer.assertValue(CalibrationState("10", device.name))
        observer.doOnChanged {
            assertEquals(CalibrationState("$idx", device.name), it)
            idx--
        }
    }

    @Test
    fun testActive() = coroutineScope.runBlockingTest {
        val observer = viewModel.calibrationActive.test()
        observer.assertValue(true)
        observer.doOnChanged {
            assertFalse(it)
        }
    }
}