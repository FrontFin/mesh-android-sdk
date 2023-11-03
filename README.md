# Mesh Connect Android SDK

Android library for integrating with Mesh Connect

## Installation

Add `link` dependency to your `build.gradle`
```gradle
dependencies {
    implementation 'com.meshconnect:link:2.0.0'
}
```

## Get Link token

Link token should be obtained from the POST `/api/v1/linktoken` endpoint. API reference for this request is available [here](https://docs.meshconnect.com/reference/post_api-v1-linktoken). The request must be performed from the server side because it requires the client's secret. You will get the response in the following format:
```json
{
  "content": {
    "linkToken": "{linkToken}"
  },
  "status": "ok",
  "message": ""
}
```

## Launch Link

Use `linkToken` to connect a brokerage account or initiate a crypto transfer

```kotlin
import com.meshconnect.link.entity.AccessTokenPayload
import com.meshconnect.link.entity.LinkPayload
import com.meshconnect.link.entity.TransferFinishedErrorPayload
import com.meshconnect.link.entity.TransferFinishedSuccessPayload
import com.meshconnect.link.ui.LinkContract
import com.meshconnect.link.ui.LinkExit
import com.meshconnect.link.ui.LinkSuccess

class LinkExampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // launch Link
        linkButton.setOnClickListener {
            linkLauncher.launch(
                "linkToken"
            )
        }
    }

    private val linkLauncher = registerForActivityResult(LinkContract()) { result ->
        when (result) {
            is LinkSuccess -> {
                handlePayloads(result.payloads)
            }

            is LinkExit -> {
                // user canceled the flow by clicking on the back or close button
                // probably because of an error. Use 'result.errorMessage' to get details.
            }
        }
    }

    private fun handlePayloads(payloads: List<LinkPayload>) {
        payloads.forEach { payload ->
            when (payload) {
                is AccessTokenPayload -> {
                    // broker connected. Use 'payload' to get details.
                }
                
                is TransferFinishedSuccessPayload -> {
                    // transfer succeed. Use 'payload' to get details.
                }
                
                is TransferFinishedErrorPayload -> {
                    // transfer failed. Use 'payload' to get details.
                }
            }
        }
    }
}
```

## Account storage

Android SDK provides built-in encrypted storage for connected accounts
```kotlin
private val accountStore: MeshAccountStore = createPreferenceAccountStore(context)
```
