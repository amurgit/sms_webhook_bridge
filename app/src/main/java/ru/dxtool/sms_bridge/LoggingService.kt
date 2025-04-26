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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
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
        val json = """
            {
                "sender": "$sender",
                "message": "$message"
            }
        """.trimIndent()

        CoroutineScope(Dispatchers.IO).launch {
            val body = RequestBody.create("application/json".toMediaTypeOrNull(), json)
            val request = Request.Builder()
                .url(webhookUrl)
                .post(body)
                .build()

            try {
                LogManager.addLog("Sending to webhook: $webhookUrl")
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    LogManager.addLog("Webhook success: HTTP ${response.code}")
                } else {
                    LogManager.addLog("Webhook failed: HTTP ${response.code}")
                }
                Log.i("LoggingService", "Webhook response: ${response.body?.string()}")
            } catch (e: Exception) {
                LogManager.addLog("Webhook error: ${e.message}")
                Log.e("LoggingService", "Error sending webhook request", e)
            }
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