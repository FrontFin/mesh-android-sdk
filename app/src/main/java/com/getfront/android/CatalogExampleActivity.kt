package com.getfront.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.getfront.android.databinding.CatalogExampleActivityBinding
import com.getfront.catalog.entity.AccessTokenPayload
import com.getfront.catalog.entity.FrontAccount
import com.getfront.catalog.entity.TransferFinishedErrorPayload
import com.getfront.catalog.entity.TransferFinishedPayload
import com.getfront.catalog.entity.TransferFinishedSuccessPayload
import com.getfront.catalog.store.createPreferenceAccountStore
import com.getfront.catalog.store.getAccountsFromPayload
import com.getfront.catalog.ui.FrontCatalogCallback
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
        /**
         * Subscribe for accounts change
         */
        subscribeAccounts()
        /**
         * Launch catalog
         */
        binding.connectBtn.setOnClickListener {
            launchCatalog(
                this,
                "catalogLink",
                getCatalogCallback()
            )
        }
    }

    private fun subscribeAccounts() {
        lifecycleScope.launch(Dispatchers.IO) {
            accountStore.accounts().collect { accounts ->
                displayAccounts(accounts)
            }
        }
    }

    private fun displayAccounts(accounts: List<FrontAccount>) {
        runOnUiThread {
            binding.accountsText.text = toString(accounts)
        }
    }

    private fun getCatalogCallback() = object : FrontCatalogCallback {

        override fun onExit() {
            log("Catalog closed")
        }

        override fun onBrokerConnected(payload: AccessTokenPayload) {
            log("Broker connected. $payload")
            saveAccounts(payload)
        }

        override fun onTransferFinished(payload: TransferFinishedPayload) {
            when (payload) {
                is TransferFinishedSuccessPayload -> {
                    log("Transfer succeed. $payload")
                }
                is TransferFinishedErrorPayload -> {
                    log("Transfer failed. $payload")
                }
            }
        }
    }

    private fun saveAccounts(payload: AccessTokenPayload) {
        lifecycleScope.launch(Dispatchers.IO) {
            val accounts = getAccountsFromPayload(payload)
            accountStore.insert(accounts)
        }
    }
}
