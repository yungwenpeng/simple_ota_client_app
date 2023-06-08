package com.example.simpleotaclient.websocket

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.simpleotaclient.CustomExpandableListAdapter
import com.example.simpleotaclient.api.OkHttpApiService
import com.example.simpleotaclient.databinding.ActivityMainBinding
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI

class MyWebSocketClient(
    private val context: Context,
    val binding: ActivityMainBinding,
    private val apiService: OkHttpApiService,
    serverUri: URI
) : WebSocketClient(serverUri) {

    override fun onOpen(handshakedata: ServerHandshake?) {
        Log.d("WebSocketClient", "WebSocketClient opened connection")
    }

    override fun onMessage(message: String?) {
        Log.d("WebSocketClient", "WebSocketClient received:$message")
        message?.let {
            val jsonObject = JSONObject(it)
            val filename = jsonObject.getString("filename")
            val size = jsonObject.getLong("size")

            when (val method = jsonObject.getString("method")) {
                "upload" -> {
                    (context as Activity).runOnUiThread {
                        val toastMessage = "Received message: $filename, size: $size, method: $method"
                        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
                        apiService.getOtaPackageInfo("all")
                    }
                }
                else -> {
                    Log.d("WebSocketClient", "WebSocketClient received (${method})")
                }
            }
        }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Log.d("WebSocketClient",
            "WebSocketClient Connection closed by " + (if (remote) "remote peer" else "us") + " Code: " + code + " Reason: "
                    + reason
        )
    }

    override fun onError(ex: Exception?) {
        Log.d("WebSocketClient", "WebSocketClient onError")
    }
}