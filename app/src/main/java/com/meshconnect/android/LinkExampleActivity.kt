package com.meshconnect.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.meshconnect.android.databinding.LinkExampleActivityBinding
import com.meshconnect.link.entity.AccessTokenPayload
import com.meshconnect.link.entity.LinkPayload
import com.meshconnect.link.entity.TransferFinishedErrorPayload
import com.meshconnect.link.entity.TransferFinishedSuccessPayload
import com.meshconnect.link.store.LinkPayloads
import com.meshconnect.link.store.createPreferenceAccountStore
import com.meshconnect.link.store.getAccountsFromPayload
import com.meshconnect.link.ui.LinkContract
import com.meshconnect.link.ui.LinkExit
import com.meshconnect.link.ui.LinkSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LinkExampleActivity : AppCompatActivity() {

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        LinkExampleActivityBinding.inflate(layoutInflater)
    }
    private val accountStore = createPreferenceAccountStore(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Subscribe for payloads
        lifecycleScope.launch(Dispatchers.IO) {
            LinkPayloads.collect { payload ->
                log("Payload received. $payload")
            }
        }

        // Launch Catalog
        binding.linkButton.setOnClickListener {
            linkLauncher.launch(
                "linkToken"
            )
        }

        // Subscribe for accounts saved into secured storage
        lifecycleScope.launch(Dispatchers.IO) {
            accountStore.accounts().collect { accounts ->
                runOnUiThread {
                    binding.accountsText.text = toString(accounts)
                }
            }
        }
    }

    private val linkLauncher = registerForActivityResult(LinkContract()) { result ->
        when (result) {
            is LinkSuccess -> {
                handlePayloads(result.payloads)
            }

            is LinkExit -> {
                // user exited the flow by clicking on the back or close button
                // probably because of an error
                log("Exited. ${result.errorMessage}")
            }
        }
    }

    private fun handlePayloads(payloads: List<LinkPayload>) {
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
