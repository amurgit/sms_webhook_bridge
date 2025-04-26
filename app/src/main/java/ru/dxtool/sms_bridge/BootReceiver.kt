package ru.dxtool.sms_bridge

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import ru.dxtool.sms_bridge.util.LogManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i("BootReceiver", "Boot completed received, starting service")

            // Start the service
            val serviceIntent = Intent(context, LoggingService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

            // This next line won't show in UI until app is opened, but will be in logs
            LogManager.addLog("Service started after device boot")
        }
    }
}