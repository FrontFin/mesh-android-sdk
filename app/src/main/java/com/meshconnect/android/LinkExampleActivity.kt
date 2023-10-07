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
import com.meshconnect.link.ui.LinkResult
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
                "aHR0cHM6Ly93ZWIuZ2V0ZnJvbnQuY29tL2IyYi1pZnJhbWUvYzQ4MDI1YWYtNWJiZC00MjRmLWI5OGUtNDcwMmMzNjgwODVmL2Jyb2tlci1jb25uZWN0P2F1dGhfY29kZT1YRWY2MnFwLWFzVWladjhJSThRQl8tU2F4Vk8wdHVUWXVSZ0pHT25YU1p4bTFfMVBCaEhuMmhpSWJZLTdLdjFxcGthRk9qWXA1UmhwenRHRUMyVl8xUQ=="
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
            is LinkResult.Success -> {
                handlePayloads(result.payloads)
            }

            is LinkResult.Cancelled -> {
                // user canceled the flow by clicking on the back or close button
                // probably because of an error
                log("Canceled. ${result.errorMessage}")
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
