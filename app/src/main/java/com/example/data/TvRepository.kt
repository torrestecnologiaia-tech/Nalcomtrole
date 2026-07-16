package com.example.data

import kotlinx.coroutines.flow.Flow

class TvRepository(private val tvDao: TvDao) {
    val allDevices: Flow<List<TvDevice>> = tvDao.getAllDevicesFlow()
    val lastConnectedDevice: Flow<TvDevice?> = tvDao.getLastConnectedDeviceFlow()
    val history: Flow<List<CommandHistory>> = tvDao.getHistoryFlow()
    val allMacros: Flow<List<Macro>> = tvDao.getAllMacrosFlow()

    suspend fun insertDevice(device: TvDevice): Long {
        return tvDao.insertDevice(device)
    }

    suspend fun updateDevice(device: TvDevice) {
        tvDao.updateDevice(device)
    }

    suspend fun deleteDevice(device: TvDevice) {
        tvDao.deleteDevice(device)
    }

    suspend fun connectToDevice(id: Int) {
        tvDao.clearLastConnected()
        tvDao.setLastConnected(id)
    }

    suspend fun disconnectDevice() {
        tvDao.clearLastConnected()
    }

    suspend fun setFavorite(id: Int, isFav: Boolean) {
        tvDao.setFavorite(id, isFav)
    }

    suspend fun addHistory(deviceName: String, command: String) {
        tvDao.insertHistory(CommandHistory(deviceName = deviceName, command = command))
    }

    suspend fun clearHistory() {
        tvDao.clearHistory()
    }

    suspend fun addMacro(macro: Macro) {
        tvDao.insertMacro(macro)
    }

    suspend fun deleteMacro(macro: Macro) {
        tvDao.deleteMacro(macro)
    }
}
