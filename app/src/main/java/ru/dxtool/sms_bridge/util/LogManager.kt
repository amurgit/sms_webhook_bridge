package ru.dxtool.sms_bridge.util

import androidx.compose.runtime.mutableStateListOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LogManager {
    // Use mutableStateListOf for Compose reactivity
    private val _logs = mutableStateListOf<String>()
    val logs = _logs

    fun addLog(message: String) {
        // Add timestamp to log entries
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _logs.add("[$timestamp] $message")
    }

    fun clear() {
        _logs.clear()
    }
}