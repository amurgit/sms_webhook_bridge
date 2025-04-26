package ru.dxtool.sms_bridge
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.dxtool.sms_bridge.ui.theme.Sms_bridgeTheme
import ru.dxtool.sms_bridge.util.Constants
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import ru.dxtool.sms_bridge.util.LogManager
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.core.content.ContextCompat
import android.Manifest
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts


class MainActivity : ComponentActivity() {

    // Modern permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, start service
            startService(Intent(this, LoggingService::class.java))
        } else {
            // Permission denied
            Toast.makeText(this, "SMS permission is required for this app to work", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request SMS permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
        } else {
            // Permission already granted, start service
            startService(Intent(this, LoggingService::class.java))
        }

        setContent {
            Sms_bridgeTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val tabs = listOf("Webhook", "Service Status")
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> WebhookTab()
                1 -> ServiceStatusTab()
            }
        }
    }
}

@Composable
fun WebhookTab() {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    // Get saved URL or use default
    val savedUrl = sharedPreferences.getString("webhook_url", Constants.DEFAULT_WEBHOOK_URL) ?: Constants.DEFAULT_WEBHOOK_URL
    var webhookUrl by remember { mutableStateOf(TextFieldValue(savedUrl)) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(Constants.WEBHOOK_PROMPT, style = MaterialTheme.typography.bodyLarge)

        BasicTextField(
            value = webhookUrl,
            onValueChange = {
                webhookUrl = it
                // Save the URL whenever it changes
                sharedPreferences.edit().putString("webhook_url", it.text).apply()
                Log.i("WebhookTab", "Webhook URL updated: ${it.text}")
            },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.primary)
                .padding(8.dp)
        )

        Button(
            onClick = {
                Log.i("WebhookTab", "Test webhook button clicked")
                isLoading = true
                testResult = null

                // Use a coroutine to test the webhook
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Create test data
                        val json = """
                            {
                                "sender": "TEST",
                                "message": "This is a test message from SMS Bridge"
                            }
                        """.trimIndent()

                        val client = OkHttpClient()
                        val body = RequestBody.create("application/json".toMediaTypeOrNull(), json)
                        val request = Request.Builder()
                            .url(webhookUrl.text)
                            .post(body)
                            .build()

                        val response = client.newCall(request).execute()

                        // Update UI on main thread
                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                val responseBody = response.body?.string() ?: "No response body"
                                testResult = "Success! Response: $responseBody"
                                Log.i("WebhookTab", "Webhook test successful: $responseBody")
                            } else {
                                testResult = "Failed: HTTP ${response.code}"
                                Log.e("WebhookTab", "Webhook test failed: HTTP ${response.code}")
                            }
                            isLoading = false
                        }
                    } catch (e: Exception) {
                        // Update UI on main thread
                        withContext(Dispatchers.Main) {
                            testResult = "Error: ${e.message}"
                            Log.e("WebhookTab", "Webhook test error", e)
                            isLoading = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Testing...")
            } else {
                Text("Test Webhook")
            }
        }

        // Display test result if available
        testResult?.let {
            val isSuccess = it.startsWith("Success")
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSuccess) Color.Green else Color.Red
            )
        }
    }
}
@Composable
fun ServiceStatusTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Service Logs", style = MaterialTheme.typography.bodyLarge)

        // Clear logs button
        Button(
            onClick = { LogManager.clear() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear Logs")
        }

        // Scrollable log display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                items(LogManager.logs) { log ->
                    Text(
                        text = log,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}