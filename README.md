## Front Finance Android SDK

Let your users to connect a brokerage accounts via Front Android SDK.

### Setup

Add `catalog` to your `build.gradle` dependencies.

```groovy
dependencies {
    implementation 'com.getfront:catalog:1.0.0-beta01'
}
```

### Launch Catalog

Connect brokerage account with `catalogLink` and receive result.

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

SDK has build-in encrypted storage which may be used for storing accounts in safe place.

```kotlin
private val accountStore: FrontAccountStore = createPreferenceAccountStore(context)
```

