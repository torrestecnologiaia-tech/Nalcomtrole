package com.example.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

class NetworkScanner {

    companion object {
        private const val TAG = "NetworkScanner"
        private const val SSDP_IP = "239.255.255.250"
        private const val SSDP_PORT = 1900
        private const val TIMEOUT_MS = 3000

        // Basic M-SEARCH request for UPnP/SSDP devices
        private const val SSDP_SEARCH_MESSAGE =
            "M-SEARCH * HTTP/1.1\r\n" +
            "HOST: $SSDP_IP:$SSDP_PORT\r\n" +
            "MAN: \"ssdp:discover\"\r\n" +
            "MX: 2\r\n" +
            "ST: ssdp:all\r\n" +
            "\r\n"
    }

    /**
     * Discovers TVs and other UPnP devices on the local network.
     * Note: This must be called from a coroutine (e.g. viewModelScope)
     */
    suspend fun discoverDevices(): List<String> = withContext(Dispatchers.IO) {
        val devicesFound = mutableListOf<String>()
        var socket: DatagramSocket? = null
        
        try {
            socket = DatagramSocket()
            socket.soTimeout = TIMEOUT_MS
            
            val address = InetAddress.getByName(SSDP_IP)
            val searchData = SSDP_SEARCH_MESSAGE.toByteArray()
            val packet = DatagramPacket(searchData, searchData.size, address, SSDP_PORT)
            
            // Send search request
            socket.send(packet)
            Log.d(TAG, "Sent SSDP M-SEARCH broadcast.")

            // Listen for responses
            val receiveBuffer = ByteArray(1024)
            while (true) {
                try {
                    val receivePacket = DatagramPacket(receiveBuffer, receiveBuffer.size)
                    socket.receive(receivePacket)
                    
                    val response = String(receivePacket.data, 0, receivePacket.length)
                    val senderIp = receivePacket.address.hostAddress
                    Log.d(TAG, "Received response from $senderIp:\n$response")
                    
                    if (senderIp != null && !devicesFound.contains(senderIp)) {
                        devicesFound.add(senderIp)
                    }
                } catch (e: SocketTimeoutException) {
                    // Expected timeout when no more devices reply within TIMEOUT_MS
                    Log.d(TAG, "SSDP Discovery finished (timeout).")
                    break
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during SSDP discovery: ${e.message}", e)
        } finally {
            socket?.close()
        }
        
        return@withContext devicesFound
    }
}
