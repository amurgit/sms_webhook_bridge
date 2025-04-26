package ru.dxtool.sms_bridge

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import ru.dxtool.sms_bridge.util.LogManager

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            LogManager.addLog("SMS broadcast received")

            // Use the Telephony API to properly reconstruct multipart messages
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

            // Group messages by sender address
            val messageMap = mutableMapOf<String, StringBuilder>()

            for (message in messages) {
                val sender = message.originatingAddress ?: "unknown"
                val body = message.messageBody

                if (!messageMap.containsKey(sender)) {
                    messageMap[sender] = StringBuilder()
                }
                messageMap[sender]?.append(body)
            }

            // Process each complete message
            for ((sender, bodyBuilder) in messageMap) {
                val completeMessage = bodyBuilder.toString()

                Log.i("SmsReceiver", "Complete SMS from $sender: $completeMessage")
                LogManager.addLog("New SMS from $sender (${completeMessage.length} chars)")

                // Forward the complete SMS to the service
                val serviceIntent = Intent(context, LoggingService::class.java)
                serviceIntent.putExtra("sender", sender)
                serviceIntent.putExtra("message", completeMessage)
                context.startService(serviceIntent)
            }
        }
    }
}