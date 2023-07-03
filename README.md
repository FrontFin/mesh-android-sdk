## Front Finance Android SDK

Let your users connect brokerage accounts via Front Android SDK.

### Installation

Add `catalog` dependency to your `build.gradle`.
```gradle
dependencies {
    implementation 'com.getfront:catalog:1.0.0-rc02'
}
```

### Launch Catalog

Use `catalogLink` to connect a brokerage account or initiate a crypto transfer.

```kotlin
import com.getfront.catalog.entity.AccessTokenPayload
import com.getfront.catalog.entity.ClosePayload
import com.getfront.catalog.entity.TransferFinishedErrorPayload
import com.getfront.catalog.entity.TransferFinishedSuccessPayload
import com.getfront.catalog.store.FrontPayloads
import com.getfront.catalog.ui.launchCatalog

class CatalogExampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launch catalog with 'catalogLink'
        binding.connectBtn.setOnClickListener {
            catalogLauncher.launch(
                "catalogLink"
            )
        }

        // Subscribe for immediate payloads
        lifecycleScope.launch(Dispatchers.IO) {
            FrontPayloads.collect { payload ->
                log("Payload received. $payload")
            }
        }
    }

    private val catalogLauncher = registerForActivityResult(
        FrontCatalogContract()
    ) { result ->
        when (result) {
            is FrontCatalogResult.Success -> {
                handlePayloads(result.payloads)
            }

            is FrontCatalogResult.Cancelled -> {
                // user cancelled the flow by clicking on back or close button
                // probably because of an error
                log("Cancelled. ${result.error?.message}")
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

    private fun log(msg: String) {
        Log.d("FrontSDK", msg)
    }
}
```

### Account storage

You may keep accounts in built-in encrypted storage.
```kotlin
private val accountStore: FrontAccountStore = createPreferenceAccountStore(context)
```
