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

#### Additional parameters (optional)

| Parameter | Type | Description                                                                                                                                                                                   |
|---|---|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `accessTokens` | `List<IntegrationAccessToken>?` | Previously obtained access tokens to pre-populate the flow. Useful in transfer flows where the source account is already authenticated. |
| `disableDomainWhiteList` | `Boolean?` | Disables origin whitelisting in the WebView. By default it's enabled with the predefined [domains](link/src/main/java/com/meshconnect/link/utils/WhitelistedOrigins.kt). Intended for testing only. |
| `language` | `String?` | BCP-47 language tag that overrides the UI locale (e.g. `"en"`, `"fr-FR"`). Pass `"system"` to use the device's current locale automatically.                                                  |
| `displayFiatCurrency` | `String?` | ISO 4217 currency code shown as the fiat equivalent of crypto amounts (e.g. `"USD"`, `"EUR"`).                                                                                                |
| `theme` | `LinkTheme?` | Colour theme of the Link UI. Accepts `LIGHT`, `DARK`, or `SYSTEM`. Pass `SYSTEM` to follow the device's setting. |

### 2. Register an Activity Result callback

The Link UI runs inside a separate Activity. Use the
[Activity Result APIs](https://developer.android.com/training/basics/intents/result) to receive
the result:

```kotlin
private val linkLauncher = registerForActivityResult(LaunchLink()) { result ->
    when (result) {
        is LinkSuccess -> { /* handle success */ }
        is LinkExit -> { /* handle exit */ }
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
            is AccessTokenPayload -> { /* broker connected */ }
            is DelayedAuthPayload -> { /* delayed authentication */ }
            is TransferFinishedSuccessPayload -> { /* transfer succeeded */ }
            is TransferFinishedErrorPayload -> { /* transfer failed */ }
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

## Returning users

To skip re-authentication for a broker a user already connected, capture the `tokenId` from
`AccountToken` in `AccessTokenPayload` on their first session and store it server-side, keyed by
user and `brokerType`:

```kotlin
is AccessTokenPayload -> payload.accountTokens.forEach { accountToken ->
    // Persist accountToken.tokenId + payload.brokerType for this user
}
```

On a later session, pass the stored `tokenId` back as the `accessToken` field of an
`IntegrationAccessToken` via `LinkConfiguration.accessTokens` to skip authentication for that
broker:

```kotlin
val configuration = LinkConfiguration(
    token = "linkToken",
    accessTokens = listOf(
        IntegrationAccessToken(
            accessToken = storedTokenId,
            brokerType = storedBrokerType,
            brokerName = "",
            accountId = "",
            accountName = "",
        ),
    ),
)
```

`tokenId` stays stable for a given user + `brokerType` combination even as the underlying access
token refreshes, so it does not need to be updated once captured. See the
[Return users guide](https://docs.meshconnect.com/build/return-users) for the full flow.

## Deep link navigation (recommended)

When the SDK opens an external browser (e.g. a Chrome Custom Tab), the return deep link
that brings the user back can restart your task and destroy whatever was on top —
typically `LinkActivity`. To resume the previous state without recreating any activity,
route the return deep link through a trampoline Activity that simply moves the existing
task back to the foreground.

**1. Define a custom URI scheme handled by a trampoline Activity.**

`AndroidManifest.xml`:

```xml
<activity
    android:name=".DeepLinkActivity"
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

**2. In the trampoline Activity, move the app's existing task to the front and finish.**

`DeepLinkActivity.kt`:

```kotlin
/**
 * Trampoline that handles the return deep link fired from an external browser
 * (e.g. a Chrome Custom Tab opened by the SDK's LinkActivity).
 *
 * It brings the app's existing task back to the foreground and resumes whatever
 * was on top — typically LinkActivity, which sits above MainActivity in the
 * task — without recreating any activity, then finishes so the resumed activity
 * shows through. Starting MainActivity via a launcher intent is deliberately
 * avoided: that intent carries FLAG_ACTIVITY_RESET_TASK_IF_NEEDED, which resets
 * the task to its root and destroys LinkActivity.
 */
class DeepLinkActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        moveAppTaskToFront()
        finish()
    }

    private fun moveAppTaskToFront() {
        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        am.appTasks.forEach {
            if (it.taskInfo.baseActivity?.className == MainActivity::class.java.name) {
                it.moveToFront()
                return
            }
        }
    }
}
```

**3. Test the deep link:**

```shell
adb shell am start -a android.intent.action.VIEW -d "myapp://"
```

When triggered:

- If the app is in the background — its existing task is brought to the foreground with the
  top activity (e.g. `LinkActivity`) resumed as-is, no recreation.
- If the app is not running — the default launcher Activity is opened.
