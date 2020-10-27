package edu.teco.earablecompanion.data

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import edu.teco.earablecompanion.data.dao.SensorDataDao
import edu.teco.earablecompanion.data.entities.SensorData
import edu.teco.earablecompanion.data.entities.SensorDataEntry
import edu.teco.earablecompanion.overview.device.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class SensorDataRepository @Inject constructor(private val sensorDataDao: SensorDataDao) {

    private val _activeRecording = MutableStateFlow<SensorDataRecording?>(null)
    val activeRecording = _activeRecording.asStateFlow()
    val isRecording: Boolean
        get() = activeRecording.value != null

    fun getSensorDataByIdFlow(id: Long): Flow<SensorData> = sensorDataDao.getDataFlow(id)
    fun getSensorDataFlow(): Flow<List<SensorData>> = sensorDataDao.getAllFlow()

    suspend fun getDataEntryCount(dataId: Long): Int = sensorDataDao.getEntryCountByDataId(dataId)
    suspend fun getSensorDataEntries(dataId: Long): List<SensorDataEntry> = sensorDataDao.getEntries(dataId)

    suspend fun clearData() = sensorDataDao.deleteAll()
    suspend fun removeData(id: Long) = sensorDataDao.delete(id)

    suspend fun startRecording(title: String, devices: List<BluetoothDevice>) {
        val data = SensorData(title = title, createdAt = LocalDateTime.now(ZoneId.systemDefault()))
        val dataId = sensorDataDao.insert(data)
        data.dataId = dataId

        val recording = SensorDataRecording(data, devices)
        _activeRecording.value = recording
    }

    suspend fun stopRecording() {
        val data = _activeRecording.value?.data ?: return
        data.stoppedAt = LocalDateTime.now(ZoneId.systemDefault())

        sensorDataDao.update(data)
        _activeRecording.value = null
    }

    suspend fun addSensorDataEntryFromCharacteristic(config: Config, characteristic: BluetoothGattCharacteristic) {
        val dataId = activeRecording.value?.data?.dataId ?: return
        val entry = config.parseSensorValues(characteristic) ?: return
        entry.dataId = dataId

        sensorDataDao.insertEntry(entry)
    }

    suspend fun updateSensorData(dataId: Long, title: String, description: String?) = sensorDataDao.updateData(dataId, title, description)

    suspend fun exportData(dataId: Long, outputStream: OutputStream) = withContext(Dispatchers.IO) {
        val entries = sensorDataDao.getEntries(dataId)
        outputStream.use { os ->
            os.bufferedWriter().use { writer ->
                writer.write(SensorDataEntry.CSV_HEADER_ROW)
                entries.sortedBy { it.timestamp }.forEach {
                    writer.write(it.asCsvEntry)
                }
            }
        }
    }

    companion object {
        private val TAG = SensorDataRecording::class.java.simpleName
    }
}