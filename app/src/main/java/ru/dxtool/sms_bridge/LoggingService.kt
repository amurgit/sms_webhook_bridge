package ru.dxtool.sms_bridge

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import ru.dxtool.sms_bridge.util.Constants
import ru.dxtool.sms_bridge.util.LogManager

class LoggingService : Service() {
    private val NOTIFICATION_ID = 1001
    private val client = OkHttpClient()

    override fun onCreate() {
        super.onCreate()
        LogManager.addLog("Service created")
        startAsForeground()
    }

    private fun startAsForeground() {
        // Create intent to open the app when notification is tapped
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification - no need for channels on Android 6
        val notification = NotificationCompat.Builder(this)
            .setContentTitle("SMS Bridge Running")
            .setContentText("Monitoring for incoming SMS messages")
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // Start as foreground service
        startForeground(NOTIFICATION_ID, notification)
        LogManager.addLog("Service started in foreground mode")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("LoggingService", "Service started")
        LogManager.addLog("Service command received")

        // Process SMS if this intent contains SMS data
        if (intent != null && intent.hasExtra("sender") && intent.hasExtra("message")) {
            val sender = intent.getStringExtra("sender") ?: ""
            val message = intent.getStringExtra("message") ?: ""

            // Get webhook URL from SharedPreferences
            val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            val webhookUrl = sharedPreferences.getString("webhook_url", Constants.DEFAULT_WEBHOOK_URL) ?: Constants.DEFAULT_WEBHOOK_URL

            LogManager.addLog("Received SMS from $sender")
            LogManager.addLog("Message: $message")

            handleIncomingSms(sender, message, webhookUrl)
        }

        return START_STICKY
    }

    private fun handleIncomingSms(sender: String, message: String, webhookUrl: String) {
        // Import these at the top of your file
        // import org.json.JSONObject
        // import okhttp3.MediaType.Companion.toMediaType
        // import okhttp3.RequestBody.Companion.toRequestBody

        try {
            // Keep your original structure
            val jsonObject = JSONObject().apply {
                put("sender", sender)
                put("message", message)
            }

            val jsonString = jsonObject.toString()
            Log.d("LoggingService", "Sending JSON: $jsonString") // Debug log

            CoroutineScope(Dispatchers.IO).launch {
                // Modern syntax, replacing the deprecated create() method
                val body = jsonString.toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(webhookUrl)
                    .post(body)
                    .build()

                try {
                    LogManager.addLog("Sending to webhook: $webhookUrl")
                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string() ?: "No response body"

                    if (response.isSuccessful) {
                        LogManager.addLog("Webhook success: HTTP ${response.code}")
                    } else {
                        LogManager.addLog("Webhook failed: HTTP ${response.code}")
                    }
                    Log.i("LoggingService", "Webhook response: $responseBody")
                } catch (e: Exception) {
                    LogManager.addLog("Webhook error: ${e.message}")
                    Log.e("LoggingService", "Error sending webhook request", e)
                }
            }
        } catch (e: Exception) {
            LogManager.addLog("Error creating JSON: ${e.message}")
            Log.e("LoggingService", "Error creating JSON payload", e)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        LogManager.addLog("Service destroyed")
    }
}