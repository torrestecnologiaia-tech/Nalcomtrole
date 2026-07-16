package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TvDao {
    // --- TvDevice Queries ---
    @Query("SELECT * FROM tv_devices ORDER BY name ASC")
    fun getAllDevicesFlow(): Flow<List<TvDevice>>

    @Query("SELECT * FROM tv_devices WHERE isLastConnected = 1 LIMIT 1")
    fun getLastConnectedDeviceFlow(): Flow<TvDevice?>

    @Query("SELECT * FROM tv_devices WHERE id = :id")
    suspend fun getDeviceById(id: Int): TvDevice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: TvDevice): Long

    @Update
    suspend fun updateDevice(device: TvDevice)

    @Delete
    suspend fun deleteDevice(device: TvDevice)

    @Query("UPDATE tv_devices SET isLastConnected = 0")
    suspend fun clearLastConnected()

    @Query("UPDATE tv_devices SET isLastConnected = 1 WHERE id = :id")
    suspend fun setLastConnected(id: Int)

    @Query("UPDATE tv_devices SET isFavorite = :isFav WHERE id = :id")
    suspend fun setFavorite(id: Int, isFav: Boolean)

    // --- CommandHistory Queries ---
    @Query("SELECT * FROM command_history ORDER BY timestamp DESC LIMIT 50")
    fun getHistoryFlow(): Flow<List<CommandHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: CommandHistory)

    @Query("DELETE FROM command_history")
    suspend fun clearHistory()

    // --- Macro Queries ---
    @Query("SELECT * FROM macros ORDER BY name ASC")
    fun getAllMacrosFlow(): Flow<List<Macro>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacro(macro: Macro)

    @Delete
    suspend fun deleteMacro(macro: Macro)
}
