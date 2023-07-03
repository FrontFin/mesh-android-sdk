package com.getfront.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.getfront.android.databinding.CatalogExampleActivityBinding
import com.getfront.catalog.entity.AccessTokenPayload
import com.getfront.catalog.entity.FrontPayload
import com.getfront.catalog.entity.TransferFinishedErrorPayload
import com.getfront.catalog.entity.TransferFinishedSuccessPayload
import com.getfront.catalog.store.FrontPayloads
import com.getfront.catalog.store.createPreferenceAccountStore
import com.getfront.catalog.store.getAccountsFromPayload
import com.getfront.catalog.ui.FrontCatalogContract
import com.getfront.catalog.ui.FrontCatalogResult
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
                log("Payload received. $payload")
            }
        }

        // Launch catalog with 'catalogLink'
        binding.connectBtn.setOnClickListener {
            catalogLauncher.launch(
                "catalogLink"
            )
        }

        // Subsctibe for accounts saved into secured storage
        lifecycleScope.launch(Dispatchers.IO) {
            accountStore.accounts().collect { accounts ->
                runOnUiThread {
                    binding.accountsText.text = toString(accounts)
                }
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
                log("Cancelled ${result.error?.message}")
            }
        }
    }

    private fun handlePayloads(payloads: List<FrontPayload>) {
        payloads.forEach { payload ->
            when (payload) {
                is AccessTokenPayload -> {
                    log("Broker connected. $payload")
                    // save accounts into secure storage (optional)
                    saveAccountsFromPayload(payload)
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

    private fun saveAccountsFromPayload(payload: AccessTokenPayload) {
        lifecycleScope.launch(Dispatchers.IO) {
            accountStore.insert(getAccountsFromPayload(payload))
        }
    }
}
