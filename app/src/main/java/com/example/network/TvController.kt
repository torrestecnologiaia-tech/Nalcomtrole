package com.example.network

/**
 * Universal interface for TV controls.
 * Different drivers (UPnP, WebOS, Tizen) can implement this interface.
 */
interface TvController {
    suspend fun volumeUp(): Boolean
    suspend fun volumeDown(): Boolean
    suspend fun setMute(mute: Boolean): Boolean
    
    suspend fun play(): Boolean
    suspend fun pause(): Boolean
    suspend fun stop(): Boolean
}
