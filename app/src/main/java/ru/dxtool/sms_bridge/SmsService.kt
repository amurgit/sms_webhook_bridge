package ru.dxtool.sms_bridge
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class SmsService : LifecycleService() {

    private val client = OkHttpClient()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d("SmsService", "Service started")
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    fun handleIncomingSms(sender: String, message: String, webhookUrl: String) {
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
                val response = client.newCall(request).execute()
                Log.d("SmsService", "Webhook response: ${response.body?.string()}")
            } catch (e: Exception) {
                Log.e("SmsService", "Error sending webhook request", e)
            }
        }
    }
}