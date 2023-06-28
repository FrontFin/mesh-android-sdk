## Front Finance Android SDK

Let your users connect brokerage accounts via Front Android SDK.

### Installation

Add `catalog` dependency to your `build.gradle`.
```gradle
dependencies {
    implementation 'com.getfront:catalog:1.0.0-rc01'
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

        // Subscribe for payloads
        lifecycleScope.launch(Dispatchers.IO) {
            FrontPayloads.collect { payload ->
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
                    is ClosePayload -> {
                        log("Catalog closed")
                    }
                }
            }
        }

        // Launch catalog with 'catalogLink'
        connectBtn.setOnClickListener {
            launchCatalog(
                this,
                "catalogLink"
            )
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
