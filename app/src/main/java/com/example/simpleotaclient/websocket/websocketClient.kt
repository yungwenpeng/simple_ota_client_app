package com.example.simpleotaclient.websocket

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.simpleotaclient.R
import com.example.simpleotaclient.api.OkHttpApiService
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI

class MyWebSocketClient(
    private val context: Context,
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

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channelId = "default_channel_id"
                        val channelName = "Default Channel"
                        val importance = NotificationManager.IMPORTANCE_HIGH
                        val channel = NotificationChannel(channelId, channelName, importance)
                        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.createNotificationChannel(channel)
                    }

                    val notificationBuilder = NotificationCompat.Builder(context, "default_channel_id")
                        .setContentTitle("New OTA Update")
                        .setContentText("Filename: $filename, Size: $size")
                        .setSmallIcon(R.drawable.notifications_48px)
                        .setAutoCancel(true)

                    val notificationManager = NotificationManagerCompat.from(context)
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return
                    }
                    notificationManager.notify(1, notificationBuilder.build())
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