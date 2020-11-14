package edu.teco.earablecompanion.data

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import edu.teco.earablecompanion.bluetooth.config.Config
import edu.teco.earablecompanion.data.dao.SensorDataDao
import edu.teco.earablecompanion.data.entities.LogEntry
import edu.teco.earablecompanion.data.entities.SensorData
import edu.teco.earablecompanion.data.entities.SensorDataEntry
import edu.teco.earablecompanion.utils.extensions.updateValue
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.FileNotFoundException
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class SensorDataRepository @Inject constructor(private val sensorDataDao: SensorDataDao, private val scope: CoroutineScope) {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, Log.getStackTraceString(throwable))
    }

    private val _activeRecording = MutableSharedFlow<SensorDataRecording?>(1, onBufferOverflow = BufferOverflow.DROP_OLDEST).apply { tryEmit(null) }
    val activeRecording: SharedFlow<SensorDataRecording?> = _activeRecording.asSharedFlow()

    val isRecording: Boolean
        get() = activeRecording.replayCache.firstOrNull() != null

    fun getSensorDataByIdFlow(id: Long): Flow<SensorData?> = sensorDataDao.getDataFlow(id)
    fun getSensorDataFlow(): Flow<List<SensorData>> = sensorDataDao.getAllFlow()

    suspend fun getDataEntryCount(dataId: Long): Int = sensorDataDao.getEntryCountByDataId(dataId)
    suspend fun getSensorDataEntries(dataId: Long): List<SensorDataEntry> = sensorDataDao.getEntries(dataId)
    suspend fun getLogEntries(dataId: Long): List<LogEntry> = sensorDataDao.getLogEntries(dataId)

    suspend fun removeAll() {
        sensorDataDao.getAll().forEach(SensorData::removeMicRecording)
        sensorDataDao.deleteAll()
    }

    suspend fun removeData(id: Long) {
        sensorDataDao.getData(id).removeMicRecording()
        sensorDataDao.delete(id)
    }

    fun startRecording(title: String, devices: List<BluetoothDevice>, micFile: File?, calibrations: List<SensorDataEntry>) = scope.launch(coroutineExceptionHandler) {
        val data = SensorData(
            title = title,
            createdAt = LocalDateTime.now(ZoneId.systemDefault()),
            micRecordingPath = micFile?.absolutePath
        )
        val dataId = sensorDataDao.insert(data)
        val mappedCalibrations = calibrations.onEach { it.dataId = dataId }
        sensorDataDao.insertEntries(mappedCalibrations)
        data.dataId = dataId

        val recording = SensorDataRecording(data, devices)
        _activeRecording.tryEmit(recording)
    }

    fun stopRecording() = scope.launch(coroutineExceptionHandler) {
        val data = _activeRecording.replayCache.firstOrNull()?.data ?: return@launch
        data.stoppedAt = LocalDateTime.now(ZoneId.systemDefault())

        sensorDataDao.update(data)
        _activeRecording.tryEmit(null)
    }

    fun addSensorDataEntryFromCharacteristic(device: BluetoothDevice, config: Config, characteristic: BluetoothGattCharacteristic) = scope.launch(coroutineExceptionHandler) {
        val dataId = activeRecording.replayCache.firstOrNull()?.data?.dataId ?: return@launch
        val entry = config.parseSensorValues(device, characteristic) ?: return@launch
        entry.dataId = dataId

        _activeRecording.updateValue {
            val existing = this?.latestValues?.get(device.address)
            this?.latestValues?.set(device.address, existing?.replaceValues(entry) ?: entry)
        }
        sensorDataDao.insertEntry(entry)
    }

    fun addLogEntry(message: String) = scope.launch(coroutineExceptionHandler) {
        val dataId = activeRecording.replayCache.firstOrNull()?.data?.dataId ?: return@launch
        val entry = LogEntry(
            dataId = dataId,
            timestamp = LocalDateTime.now(ZoneId.systemDefault()),
            message = message
        )

        sensorDataDao.insertLogEntry(entry)
    }

    fun addMediaButtonEntry(pressed: Int) = scope.launch(coroutineExceptionHandler) {
        val dataId = activeRecording.replayCache.firstOrNull()?.data?.dataId ?: return@launch
        val entry = SensorDataEntry(
            dataId = dataId,
            timestamp = LocalDateTime.now(ZoneId.systemDefault()),
            buttonPressed = pressed
        )

        sensorDataDao.insertEntry(entry)
    }

    suspend fun hasLogs(dataId: Long): Boolean = sensorDataDao.getLogEntryCountByDataId(dataId) > 0

    suspend fun updateSensorData(dataId: Long, title: String) = sensorDataDao.updateData(dataId, title)

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun exportData(dataId: Long, outputStream: OutputStream) = withContext(Dispatchers.IO) {
        val calibrationEntries = sensorDataDao.getCalibrationEntries(dataId).sortedBy { it.timestamp }
        val entries = sensorDataDao.getEntries(dataId).sortedBy { it.timestamp }
        outputStream.sink().buffer().use { sink ->
            sink.writeUtf8(SensorDataEntry.CSV_HEADER_ROW)
            calibrationEntries.forEach { sink.writeUtf8(it.asCsvEntry) }
            entries.forEach { sink.writeUtf8(it.asCsvEntry) }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun exportMicRecording(dataId: Long, outputStream: OutputStream) = withContext(Dispatchers.IO) {
        val data = sensorDataDao.getData(dataId)
        val path = data.micRecordingPath ?: throw FileNotFoundException()

        outputStream.sink().buffer().use { sink ->
            File(path).source().buffer().use { source ->
                source.readAll(sink)
            }
        }
    }

    private fun SensorDataEntry.replaceValues(other: SensorDataEntry) = copy(
        accX = other.accX ?: this.accX,
        accY = other.accY ?: this.accY,
        accZ = other.accZ ?: this.accZ,
        gyroX = other.gyroX ?: this.gyroX,
        gyroY = other.gyroY ?: this.gyroY,
        gyroZ = other.gyroZ ?: this.gyroZ,
        buttonPressed = other.buttonPressed ?: this.buttonPressed,
        heartRate = other.heartRate ?: this.heartRate,
        bodyTemperature = other.bodyTemperature ?: this.bodyTemperature,
        oxygenSaturation = other.oxygenSaturation ?: this.oxygenSaturation,
        pulseRate = other.pulseRate ?: this.pulseRate
    )

    companion object {
        private val TAG = SensorDataRecording::class.java.simpleName
    }
}