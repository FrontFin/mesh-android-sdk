package com.getfront.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.getfront.android.databinding.CatalogExampleActivityBinding
import com.getfront.catalog.entity.AccessTokenPayload
import com.getfront.catalog.entity.ClosePayload
import com.getfront.catalog.entity.TransferFinishedErrorPayload
import com.getfront.catalog.entity.TransferFinishedSuccessPayload
import com.getfront.catalog.store.FrontPayloads
import com.getfront.catalog.store.createPreferenceAccountStore
import com.getfront.catalog.store.getAccountsFromPayload
import com.getfront.catalog.ui.launchCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CatalogExampleActivity : AppCompatActivity() {

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        CatalogExampleActivityBinding.inflate(layoutInflater)
    }
    private val accountStore = createPreferenceAccountStore(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Subscribe for payloads
        lifecycleScope.launch(Dispatchers.IO) {
            FrontPayloads.collect { payload ->
                when (payload) {
                    is AccessTokenPayload -> {
                        log("Broker connected. $payload")

                        // save accounts into secure storage (optional)
                        accountStore.insert(getAccountsFromPayload(payload))
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
        binding.connectBtn.setOnClickListener {
            launchCatalog(
                this,
                "catalogLink",
            )
        }

        // Subscribe for accounts & display
        lifecycleScope.launch(Dispatchers.IO) {
            accountStore.accounts().collect { accounts ->
                runOnUiThread {
                    binding.accountsText.text = toString(accounts)
                }
            }
        }
    }
}
