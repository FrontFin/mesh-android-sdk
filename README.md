## Front Finance Android SDK

Let your users connect brokerage accounts via Front Android SDK.

### Installation

Add `catalog` to your `build.gradle`.
```groovy
dependencies {
    implementation 'com.getfront:catalog:1.0.0-beta02'
}
```

### Launch Catalog

Use `catalogLink` to connect your brokerage account.
```kotlin
class ConnectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connectBtn.setOnClickListener {
            catalogLauncher.launch("catalogLink")
        }
    }

    private val catalogLauncher = registerForActivityResult(
        FrontCatalogContract()
    ) {
        Log.i("FrontAccounts", it.toString())
    }
}
```

### Store accounts

Built-in encrypted storage allows you to store accounts in a safe place.
```kotlin
private val accountStore: FrontAccountStore = createPreferenceAccountStore(context)
```
