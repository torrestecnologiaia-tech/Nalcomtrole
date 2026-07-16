package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tv_devices")
data class TvDevice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val brand: String,
    val ipAddress: String,
    val isFavorite: Boolean = false,
    val isLastConnected: Boolean = false,
    val connectionType: String = "Wi-Fi" // "Wi-Fi", "IR", "Bluetooth"
)

@Entity(tableName = "command_history")
data class CommandHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deviceName: String,
    val command: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "macros")
data class Macro(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val commands: String, // comma-separated values, e.g. "POWER,NETFLIX,VOLUME_UP"
    val isFavorite: Boolean = false
)
