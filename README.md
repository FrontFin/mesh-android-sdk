# Mesh Connect Android SDK

Android library for integrating with Mesh Connect.

[![Maven Central](https://img.shields.io/maven-central/v/com.meshconnect/link?color=%23037FFF&link=https%3A%2F%2Fsearch.maven.org%2Fartifact%2Fcom.meshconnect%2Flink)](https://central.sonatype.com/artifact/com.meshconnect/link)

## Installation

Add dependency to your `build.gradle`:

```gradle
dependencies {
    implementation 'com.meshconnect:link:$linkVersion'
}
```

## Getting Link token

The `linkToken` should be obtained from
the [`/api/v1/linktoken`](https://docs.meshconnect.com/reference/post_api-v1-linktoken) endpoint.
The request must be performed from the server side as it risks exposing your API secret.
You will get the response in the following format:

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

### Create a LinkConfiguration

Each time you launch Link, you have to get a new `linkToken` from your backend and build a new
`LinkConfiguration` object:

```kotlin
val configuration = LinkConfiguration(
    token = "linkToken"
)
```

Additional `LinkConfiguration` object parameters:

- `accessTokens` to initialize crypto transfers flow at the 'Select asset step’ using previously obtained integration `auth_token`. 
It can be used if you have a valid `auth_token` and want to bypass authentication to jump right into a transfer.

- `transferDestinationTokens` for crypto transfers flow. 
It is an alternative way of providing target addresses for crypto transfers by using previously obtained integration `auth_tokens`.

- `disableDomainWhiteList` is a flag that allows to disable origin whitelisting.
By default, it's enabled with the predefined [domains](link/src/main/java/com/meshconnect/link/utils/WhitelistedvOrigins.kt).

### Register an Activity Result callback

The Link UI runs in a separate Activity within your app.
To return the result you can use [Activity Result APIs](https://developer.android.com/training/basics/intents/result).

```kotlin
private val linkLauncher = registerForActivityResult(LaunchLink()) { result ->
    when (result) {
        is LinkSuccess -> /* handle success */
        is LinkExit -> /* handle exit */
    }
}
```

### Launch Link

```kotlin
linkLauncher.launch(configuration)
```

At this point, Link UI will open and return the `LinkSuccess` object if the user successfully
completes the flow.

### LinkSuccess

When a user successfully links an account or completes the transfer, the `LinkSuccess` object is
received. It contains a list of payloads that represent the linked items:

```kotlin
private fun onLinkSuccess(result: LinkSuccess) {
    result.payloads.forEach { payload ->
        when (payload) {
            is AccessTokenPayload -> /* broker connected */
            is DelayedAuthPayload -> /* delayed authentication */
            is TransferFinishedSuccessPayload -> /* transfer succeed */
            is TransferFinishedErrorPayload -> /* transfer failed */
        }
    }
}
```

### LinkExit

When a user exits Link without successfully linking an account or an error occurs, 
the `LinkExit` object is received:

```kotlin

private fun onLinkExit(result: LinkExit) {
    if (result.errorMessage != null) {
        /* use error message */
    }
}
```

### LinkPayloads

A `SharedFlow` emits payloads immediately:

```kotlin
lifecycleScope.launch {
    LinkPayloads.collect { /* use payloads */ }
}
```

### LinkEvents

A `SharedFlow` emits events that happen at certain points in the Link flow:

```kotlin
lifecycleScope.launch {
  LinkEvents.collect { /* use event */ }
}
```

## Deeplink navigation (recommended solution)

Standard deep links always create a new task or activity, unless you handle the back stack yourself.
To resume the previous state, consider these steps:

1. Define a custom URI scheme that is handled by a "No-op" activity.
2. No-op activity checks if the app is already running. If so, it finishes itself.

AndroidManifest.xml:
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

DeepLinkEntryActivity.kt:
```kotlin
class DeepLinkEntryActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Check if app is already running
        packageManager.getLaunchIntentForPackage(packageName)?.run {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(this)
        }
        finish()
    }
}
```

Test deeplink:
```shell
adb shell am start -a android.intent.action.VIEW -d "myapp://"
```

When triggered:
- If your app is in the background: it’s brought to the foreground.
- If it’s not running: the default launcher activity is opened.
