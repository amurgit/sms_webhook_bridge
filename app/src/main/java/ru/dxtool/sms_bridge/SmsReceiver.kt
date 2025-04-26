package ru.dxtool.sms_bridge

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log
import ru.dxtool.sms_bridge.util.LogManager

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            LogManager.addLog("SMS broadcast received")

            val bundle = intent.extras
            if (bundle != null) {
                val pdus = bundle.get("pdus") as Array<*>
                for (pdu in pdus) {
                    val message = SmsMessage.createFromPdu(pdu as ByteArray)
                    val sender = message.originatingAddress
                    val body = message.messageBody

                    Log.i("SmsReceiver", "SMS received from $sender: $body")
                    LogManager.addLog("New SMS from $sender")

                    // Forward the SMS to the service
                    val serviceIntent = Intent(context, LoggingService::class.java)
                    serviceIntent.putExtra("sender", sender)
                    serviceIntent.putExtra("message", body)
                    context.startService(serviceIntent)
                }
            }
        }
    }
}