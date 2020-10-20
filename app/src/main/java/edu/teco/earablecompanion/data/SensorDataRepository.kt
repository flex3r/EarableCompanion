package edu.teco.earablecompanion.data

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import edu.teco.earablecompanion.data.dao.SensorDataDao
import edu.teco.earablecompanion.data.entities.SensorData
import edu.teco.earablecompanion.data.entities.SensorDataEntry
import edu.teco.earablecompanion.data.entities.SensorDataWithEntries
import edu.teco.earablecompanion.overview.device.Config
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class SensorDataRepository @Inject constructor(private val sensorDataDao: SensorDataDao) {

    private val _activeRecording = MutableStateFlow<SensorDataRecording?>(null)
    val activeRecording: StateFlow<SensorDataRecording?> = _activeRecording
    val isRecording: Boolean
        get() = activeRecording.value != null

    suspend fun getAllSensorData(): List<SensorData> = sensorDataDao.getAll()
    suspend fun getSensorDataById(id: Long): SensorData = sensorDataDao.getById(id)
    fun getSensorDataFlow(): Flow<List<SensorData>> = sensorDataDao.getAllFlow()

    suspend fun getDataEntryCount(dataId: Long): Int = sensorDataDao.getEntryCountByDataId(dataId)
    suspend fun getSensorDataWithEntries(dataId: Long): Flow<SensorDataWithEntries> = sensorDataDao.getDataWithEntriesById(dataId)

    suspend fun insertAll(data: List<SensorData>) = sensorDataDao.insertAll(data)
    suspend fun insertAllEntries(entries: List<SensorDataEntry>) = sensorDataDao.insertAllEntries(entries)
    suspend fun clearData() = sensorDataDao.deleteAll()
    suspend fun removeData(id: Long) = sensorDataDao.deleteById(id)

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

    suspend fun updateSensorDataDescription(dataId: Long, text: String?) = sensorDataDao.updateDescription(dataId, text)

    companion object {
        private val TAG = SensorDataRecording::class.java.simpleName
    }
}