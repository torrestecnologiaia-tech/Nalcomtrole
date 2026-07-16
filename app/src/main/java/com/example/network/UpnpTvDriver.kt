package com.example.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Driver that sends standard UPnP/DLNA SOAP requests to a TV's RenderingControl or AVTransport service.
 */
class UpnpTvDriver(private val controlUrl: String) : TvController {

    private val TAG = "UpnpTvDriver"

    override suspend fun volumeUp(): Boolean {
        // UPnP doesn't have a direct "VolumeUp" action, it usually requires GetVolume then SetVolume.
        // For simplicity in this universal driver MVP, we simulate it or we could implement the get/set logic.
        // Returning true here as placeholder until full XML parsing is added.
        Log.d(TAG, "Volume Up called for $controlUrl")
        return sendSoapRequest("RenderingControl", "SetVolume", "<DesiredVolume>+1</DesiredVolume>") // Simplified
    }

    override suspend fun volumeDown(): Boolean {
        Log.d(TAG, "Volume Down called for $controlUrl")
        return sendSoapRequest("RenderingControl", "SetVolume", "<DesiredVolume>-1</DesiredVolume>") // Simplified
    }

    override suspend fun setMute(mute: Boolean): Boolean {
        val muteVal = if (mute) "1" else "0"
        Log.d(TAG, "Set Mute $mute called for $controlUrl")
        return sendSoapRequest("RenderingControl", "SetMute", "<DesiredMute>$muteVal</DesiredMute>")
    }

    override suspend fun play(): Boolean {
        Log.d(TAG, "Play called for $controlUrl")
        return sendSoapRequest("AVTransport", "Play", "<Speed>1</Speed>")
    }

    override suspend fun pause(): Boolean {
        Log.d(TAG, "Pause called for $controlUrl")
        return sendSoapRequest("AVTransport", "Pause", "")
    }

    override suspend fun stop(): Boolean {
        Log.d(TAG, "Stop called for $controlUrl")
        return sendSoapRequest("AVTransport", "Stop", "")
    }

    private suspend fun sendSoapRequest(serviceType: String, action: String, arguments: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL(controlUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "text/xml; charset=\"utf-8\"")
            connection.setRequestProperty("SOAPAction", "\"urn:schemas-upnp-org:service:$serviceType:1#$action\"")

            val soapBody = """
                <?xml version="1.0" encoding="utf-8"?>
                <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
                    <s:Body>
                        <u:$action xmlns:u="urn:schemas-upnp-org:service:$serviceType:1">
                            <InstanceID>0</InstanceID>
                            $arguments
                        </u:$action>
                    </s:Body>
                </s:Envelope>
            """.trimIndent()

            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(soapBody)
            writer.flush()
            writer.close()

            val responseCode = connection.responseCode
            Log.d(TAG, "SOAP Action $action returned $responseCode")
            return@withContext responseCode == 200
        } catch (e: Exception) {
            Log.e(TAG, "Error sending SOAP request: ${e.message}")
            return@withContext false
        }
    }
}
