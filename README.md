# Mesh Connect Android SDK

Android library for integrating with Mesh Connect.

![Maven Central](https://img.shields.io/maven-central/v/com.meshconnect/link?color=%23037FFF&link=https%3A%2F%2Fsearch.maven.org%2Fartifact%2Fcom.meshconnect%2Flink)

## Installation

Add dependency to your `build.gradle`:

```gradle
dependencies {
    implementation 'com.meshconnect:link:$linkVersion'
}
```

## Getting Link token

Link token should be obtained from
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

Each time you launch Link, you will need to get a new `linkToken` from your server and create a new
`LinkConfiguration` object with it:

```kotlin
val configuration = LinkConfiguration(
    token = "linkToken",
)
```

The `LinkConfiguration` class allows to add:

- `accessTokens` - list of `IntegrationAccessToken`s that used as an origin for crypto transfer
  flow;
- `transferDestinationTokens` - list of `IntegrationAccessToken`s that used as a destination for
  crypto transfer flow.

### Register an Activity Result callback

The Link SDK runs as a separate Activity within your app.
In order to return the result to your app it supports
the [Activity Result APIs](https://developer.android.com/training/basics/intents/result).

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
linkLauncher.launch(
    LinkConfig(
        token = "linkToken"
    )
)
```

At this point, Link will open, and will return the `LinkSuccess` object if the user successfully
completes the Link flow.

### LinkSuccess

When a user successfully links an account or completes the transfer, the `LinkSuccess` object is
received. It contains a list of payloads that represents the linked items:

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

When a user exits Link without successfully linking an account or an error occurs, the `LinkExit`
object is received:

```kotlin

private fun onLinkExit(result: LinkExit) {
    if (result.errorMessage != null) {
        /* use error message */
    }
}
```

### LinkPayloads

A `SharedFlow` emits payloads immediately, 
when a user successfully links an account or completes the transfer:

```kotlin
lifecycleScope.launch {
    LinkPayloads.collect { /* use hayloads */ }
}
```

### LinkEvents

A `SharedFlow` emits events that happen at certain points in the Link flow:

```kotlin
lifecycleScope.launch {
  LinkEvents.collect { /* use event */ }
}
```

## Storing the linked accounts

Android SDK provides built-in encrypted storage for linked accounts:

```kotlin
private val accountStore: MeshAccountStore = createPreferenceAccountStore(context)
```
