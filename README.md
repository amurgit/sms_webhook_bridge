# SMS Bridge for Android

A lightweight Android application that forwards incoming SMS messages to a configurable webhook URL. This tool was created to solve a specific problem: managing multiple SIM cards without needing multiple phones actively in use.

## The Problem This Solves

If you have multiple SIM cards but don't want to carry multiple phones or constantly swap SIMs, this app provides a simple solution. Install it on an old Android phone, insert the SIM card you want to monitor, configure the webhook URL, plug in a power adapter, set the phone to never sleep, and put it on a shelf. All SMS messages from that SIM will be automatically forwarded to your webhook endpoint.

## ⚠️ Compatibility Notice

**This app is designed for older Android devices (tested on Android 9 and below).** Due to increasingly restrictive permissions on newer Android versions (10+), the app may not function properly on modern devices. Google has significantly limited background SMS access for security and privacy reasons.

## Features

- 📱 Receives all incoming SMS messages
- 🔗 Forwards SMS to a configurable webhook URL via HTTP POST
- 🧪 Built-in webhook testing functionality
- 📊 Real-time service logs and status monitoring
- 🚀 Persistent foreground service to ensure reliability
- 🔄 Auto-start on device boot (experimental)
- 💾 Persistent webhook URL configuration

## How It Works

1. The app runs as a foreground service and listens for incoming SMS messages
2. When an SMS is received, it's automatically forwarded to your configured webhook URL
3. The message is sent as a JSON payload:
```json
{
  "sender": "+1234567890",
  "message": "Your SMS message content"
}
```

## Installation

1. Download the APK from the [Releases](../../releases) page
2. Enable "Install from Unknown Sources" in your Android settings
3. Install the APK
4. Grant SMS permissions when prompted
5. Configure your webhook URL in the app

## Setup

### On the Android Device

1. Open the app
2. Navigate to the "Webhook" tab
3. Enter your webhook endpoint URL
4. Use the "Test Webhook" button to verify connectivity
5. Configure the phone to never sleep (Settings → Display → Sleep → Never)
6. Plug in a power adapter
7. Check the "Service Status" tab to monitor incoming messages

### Setting Up the Webhook Receiver

You need a service that can receive webhook POST requests. There are many options:

- **Feishu (Lark)** - Chinese chat app that allows you to configure webhooks and automated responses without needing your own server. It's free and offers good automation capabilities.
- **Discord** - Create a webhook in any Discord channel
- **Slack** - Set up incoming webhooks
- **Telegram** - Use a bot to receive messages
- **n8n, Zapier, Make** - Automation platforms with webhook triggers
- **Your own server** - Any HTTP endpoint that can receive POST requests

### Example: Using Feishu/Lark

1. Create a group in Feishu/Lark
2. Add a bot to the group
3. Configure the bot's webhook URL
4. Copy the webhook URL and paste it into the SMS Bridge app
5. Configure any automation rules you want in Feishu to process the incoming SMS messages

## Technical Details

### Project Structure

- **MainActivity.kt** - Main UI with Jetpack Compose, handles webhook configuration and testing
- **SmsReceiver.kt** - BroadcastReceiver that intercepts incoming SMS messages
- **LoggingService.kt** - Foreground service that forwards SMS to webhook
- **BootReceiver.kt** - Attempts to auto-start service on device boot
- **LogManager.kt** - Reactive logging system for UI feedback

### Permissions Required

- `RECEIVE_SMS` - To intercept incoming SMS messages
- `FOREGROUND_SERVICE` - To run reliably in the background
- `RECEIVE_BOOT_COMPLETED` - For auto-start functionality (may not work on all devices)

## Known Limitations

- **Android 10+**: Google's restrictions on background SMS access may prevent the app from working
- **Auto-start**: Boot receiver may not work on all devices due to manufacturer restrictions
- **Battery optimization**: Some devices may kill the service to save battery; you may need to disable battery optimization for this app

## Potential Improvements

Here are some ideas for future development:

### Security & Privacy
- Add webhook authentication (API keys, Bearer tokens)
- Implement HTTPS certificate pinning
- Add message filtering by sender (whitelist/blacklist)
- Encrypt messages before sending
- Add optional message content redaction for sensitive data

### Functionality
- Support for multiple webhook URLs with routing rules
- Retry mechanism for failed webhook deliveries
- Queue messages when offline and send when connection restored
- Support for two-way messaging (send SMS via API)
- Export/import configuration
- Support for MMS messages

### User Experience
- Dark mode theme
- Message statistics and analytics
- Search and filter in logs
- Notification customization
- Better battery optimization handling
- Interactive setup wizard for first-time users

### Technical Improvements
- Add unit tests and integration tests
- Implement proper dependency injection
- Add crash reporting (Firebase Crashlytics or similar)
- Improve error handling and user feedback
- Add Doze mode compatibility for newer Android versions
- Implement WorkManager for better background task handling
- Add database for message history

### Modern Android Compatibility
- Investigate alternatives for Android 10+ (though this may be fundamentally limited by OS restrictions)
- Implement SMS retriever API for one-time passwords
- Add support for RCS messages where available

## Contributing

Contributions are welcome! This is my first Android project, so I'm sure there's room for improvement. Feel free to:

- Report bugs
- Suggest features
- Submit pull requests
- Improve documentation

## License

This project is licensed under the MIT License - see below:

```
MIT License

Copyright (c) 2025

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## Disclaimer

This app is provided as-is for personal use. The author is not responsible for any misuse or any consequences of using this application. Always respect privacy laws and regulations in your jurisdiction when intercepting and forwarding SMS messages.

## Support

If you find this tool useful, consider:
- ⭐ Starring the repository
- 🐛 Reporting issues
- 💡 Sharing your use cases
- 🔧 Contributing improvements

---

**Built with**: Kotlin, Jetpack Compose, OkHttp, Coroutines
