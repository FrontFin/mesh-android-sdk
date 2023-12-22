# Mesh Connect Android SDK

Android library for integrating with Mesh Connect.

## Installation

Add dependency to your `build.gradle`:

![Maven Central](https://img.shields.io/maven-central/v/com.meshconnect/link?color=%23037FFF&link=https%3A%2F%2Fsearch.maven.org%2Fartifact%2Fcom.meshconnect%2Flink)
```gradle
dependencies {
    implementation 'com.meshconnect:link:$linkVersion'
}
```

## Getting Link token

Link token should be obtained from the POST [`/api/v1/linktoken`](https://docs.meshconnect.com/reference/post_api-v1-linktoken) endpoint.
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
In order to return the result to your app it supports the [Activity Result APIs](https://developer.android.com/training/basics/intents/result).

```kotlin
private val linkLauncher = registerForActivityResult(LaunchLink()) { result ->
    when (result) {
        is LinkSuccess -> handlePayloads(result.payloads)
        is LinkExit -> {
            // user exited the flow by clicking on the back or close button
            // or an error occured, check 'result.errorMessage'
        }
    }
}

private fun handlePayloads(payloads: List<LinkPayload>) {
    payloads.forEach { payload ->
        when (payload) {
            is AccessTokenPayload -> /* broker connected */
            is DelayedAuthPayload -> /* delayed authentication */
            is TransferFinishedSuccessPayload -> /* transfer succeed */
            is TransferFinishedErrorPayload -> /* transfer failed */
        }
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

## Storing the linked accounts

Android SDK provides built-in encrypted storage for linked accounts:
```kotlin
private val accountStore: MeshAccountStore = createPreferenceAccountStore(context)
```
