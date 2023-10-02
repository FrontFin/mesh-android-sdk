## Front Finance Android SDK

Let your users connect brokerage accounts via Front Android SDK.

### Installation

Add `catalog` dependency to your `build.gradle`.
```gradle
dependencies {
    implementation 'com.getfront:catalog:1.0.1'
}
```

### Get Link token

Link token should be obtained from the POST `/api/v1/linktoken` endpoint. Api reference for this request is available [here](https://integration-api.getfront.com/apireference#tag/Managed-Account-Authentication/paths/~1api~1v1~1linktoken/post). Request must be preformed from the server side because it requires the client secret. You will get the response in the following format:
```json
{
  "content": {
    "linkToken": "{linkToken}"
  },
  "status": "ok",
  "message": ""
}
```

### Launch Catalog

Use `linkToken` to connect a brokerage account or initiate a crypto transfer.

```kotlin
import com.getfront.catalog.entity.AccessTokenPayload
import com.getfront.catalog.entity.FrontPayload
import com.getfront.catalog.entity.TransferFinishedErrorPayload
import com.getfront.catalog.entity.TransferFinishedSuccessPayload
import com.getfront.catalog.ui.FrontCatalogResult
import com.getfront.catalog.ui.FrontLinkContract

class CatalogExampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launch Front Catalog
        connectBtn.setOnClickListener {
            catalogLauncher.launch(
                "linkToken"
            )
        }
    }

    private val catalogLauncher = registerForActivityResult(
        FrontLinkContract()
    ) { result ->
        when (result) {
            is FrontCatalogResult.Success -> {
                handlePayloads(result.payloads)
            }

            is FrontCatalogResult.Cancelled -> {
                // user canceled the flow by clicking on the back or close button
                // probably because of an error
                log("Canceled. ${result.error?.message}")
            }
        }
    }

    private fun handlePayloads(payloads: List<FrontPayload>) {
        payloads.forEach { payload ->
            when (payload) {
                is AccessTokenPayload -> {
                    log("Broker connected. $payload")
                }
                
                is TransferFinishedSuccessPayload -> {
                    log("Transfer succeed. $payload")
                }
                
                is TransferFinishedErrorPayload -> {
                    log("Transfer failed. $payload")
                }
            }
        }
    }

    private fun log(msg: String) = Log.d("FrontCatalogResult", msg)
}
```

### Account storage

You may keep accounts in built-in encrypted storage.
```kotlin
private val accountStore: FrontAccountStore = createPreferenceAccountStore(context)
```
