# Mesh Connect Android SDK

Android library for integrating with Mesh Connect.

[![Maven Central](https://img.shields.io/maven-central/v/com.meshconnect/link?color=%23037FFF&link=https%3A%2F%2Fsearch.maven.org%2Fartifact%2Fcom.meshconnect%2Flink)](https://central.sonatype.com/artifact/com.meshconnect/link)

## Installation

Add the dependency to your `build.gradle`:

```gradle
dependencies {
    implementation 'com.meshconnect:link:$linkVersion'
}
```

## Getting a link token

The `linkToken` must be obtained from the
[`linktoken`](https://docs.meshconnect.com/api-reference/managed-account-authentication/get-link-token-with-parameters) endpoint.
This request must be performed server-side to avoid exposing your API secret.
The response has the following format:

```json
{
  "content": {
    "linkToken": "{linkToken}"
  },
  "status": "ok",
  "message": ""
}
```

## Launching Link

### 1. Create a `LinkConfiguration`

Each time you launch Link, obtain a fresh `linkToken` from your backend and build a
`LinkConfiguration`:

```kotlin
val configuration = LinkConfiguration(
    token = "linkToken"
)
```

#### Additional parameters

| Parameter | Type | Description |
|---|---|---|
| `accessTokens` | `List<IntegrationAccessToken>?` | Previously obtained access tokens to pre-populate the flow. Useful in transfer flows where the source account is already authenticated. |
| `disableDomainWhiteList` | `Boolean?` | Disables origin whitelisting in the WebView. By default whitelisting is enabled with the predefined [domains](link/src/main/java/com/meshconnect/link/utils/WhitelistedOrigins.kt). Intended for testing only. |
| `language` | `String?` | BCP-47 language tag that overrides the UI locale (e.g. `"en"`, `"fr-FR"`). Pass `"system"` to use the device's current locale automatically. |
| `displayFiatCurrency` | `String?` | ISO 4217 currency code shown as the fiat equivalent of crypto amounts (e.g. `"USD"`, `"EUR"`). |
| `theme` | `LinkTheme?` | Colour theme of the Link UI. Accepts `LinkTheme.LIGHT`, `LinkTheme.DARK`, or `LinkTheme.SYSTEM`. `SYSTEM` follows the device's dark-mode setting at launch. |

### 2. Register an Activity Result callback

The Link UI runs inside a separate Activity. Use the
[Activity Result APIs](https://developer.android.com/training/basics/intents/result) to receive
the result:

```kotlin
private val linkLauncher = registerForActivityResult(LaunchLink()) { result ->
    when (result) {
        is LinkSuccess -> /* handle success */
        is LinkExit -> /* handle exit */
    }
}
```

### 3. Launch Link

```kotlin
linkLauncher.launch(configuration)
```

The Link UI opens and returns a `LinkSuccess` object when the user successfully completes the flow.

### Handling results

#### `LinkSuccess`

Returned when a user links an account or completes a transfer. Contains a list of payloads:

```kotlin
private fun onLinkSuccess(result: LinkSuccess) {
    result.payloads.forEach { payload ->
        when (payload) {
            is AccessTokenPayload -> /* broker connected */
            is DelayedAuthPayload -> /* delayed authentication */
            is TransferFinishedSuccessPayload -> /* transfer succeeded */
            is TransferFinishedErrorPayload -> /* transfer failed */
        }
    }
}
```

#### `LinkExit`

Returned when a user exits Link without completing the flow, or when an error occurs:

```kotlin
private fun onLinkExit(result: LinkExit) {
    if (result.errorMessage != null) {
        /* use error message */
    }
}
```

### Real-time streams

#### `LinkPayloads`

A `SharedFlow` that emits payloads as they arrive, independently of the Activity result:

```kotlin
lifecycleScope.launch {
    LinkPayloads.collect { /* use payload */ }
}
```

#### `LinkEvents`

A `SharedFlow` that emits raw events at each step of the Link flow:

```kotlin
lifecycleScope.launch {
    LinkEvents.collect { /* use event */ }
}
```

## Deep link navigation (recommended)

Standard deep links always create a new task or activity unless you manage the back stack manually.
To resume the previous state, follow these steps:

**1. Define a custom URI scheme handled by a no-op Activity.**

`AndroidManifest.xml`:

```xml
<activity
    android:name=".DeepLinkEntryActivity"
    android:exported="true"
    android:theme="@android:style/Theme.Translucent.NoTitleBar">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="myapp" />
    </intent-filter>
</activity>
```

**2. In the no-op Activity, check whether the app is already running and finish immediately.**

`DeepLinkEntryActivity.kt`:

```kotlin
class DeepLinkEntryActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        packageManager.getLaunchIntentForPackage(packageName)?.run {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(this)
        }
        finish()
    }
}
```

**3. Test the deep link:**

```shell
adb shell am start -a android.intent.action.VIEW -d "myapp://"
```

When triggered:

- If the app is in the background — it is brought to the foreground.
- If the app is not running — the default launcher Activity is opened.
