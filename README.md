## Mesh Connect Android SDK

Let your users connect brokerage accounts via Mesh Android SDK.

### Installation

Add `link` dependency to your `build.gradle`.
```gradle
dependencies {
    implementation 'com.meshconnect:link:2.0.0'
}
```

### Get Link token

Link token should be obtained from the POST `/api/v1/linktoken` endpoint. Api reference for this request is available [here](https://integration-api.meshconnect.com/apireference#tag/Managed-Account-Authentication/paths/~1api~1v1~1linktoken/post). Request must be preformed from the server side because it requires the client secret. You will get the response in the following format:
```json
{
  "content": {
    "linkToken": "{linkToken}"
  },
  "status": "ok",
  "message": ""
}
```

### Launch Link

Use `linkToken` to connect a brokerage account or initiate a crypto transfer.

```kotlin
import com.meshconnect.link.entity.AccessTokenPayload
import com.meshconnect.link.entity.LinkPayload
import com.meshconnect.link.entity.TransferFinishedErrorPayload
import com.meshconnect.link.entity.TransferFinishedSuccessPayload
import com.meshconnect.link.ui.LinkResult
import com.meshconnect.link.ui.LinkTokenContract

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

    private val linkLauncher = registerForActivityResult(
        LinkTokenContract()
    ) { result ->
        when (result) {
            is LinkResult.Success -> {
                handlePayloads(result.payloads)
            }

            is LinkResult.Cancelled -> {
                // user canceled the flow by clicking on the back or close button
                // probably because of an error
                log("Canceled. ${result.error?.message}")
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

### Use account storage

Keep accounts in built-in encrypted storage.
```kotlin
private val accountStore: MeshAccountStore = createPreferenceAccountStore(context)
```
