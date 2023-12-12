package com.meshconnect.android

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.meshconnect.android.databinding.LinkExampleActivityBinding
import com.meshconnect.link.entity.AccessTokenPayload
import com.meshconnect.link.entity.LinkConfiguration
import com.meshconnect.link.entity.LinkPayload
import com.meshconnect.link.entity.TransferFinishedErrorPayload
import com.meshconnect.link.entity.TransferFinishedSuccessPayload
import com.meshconnect.link.store.LinkPayloads
import com.meshconnect.link.store.createPreferenceAccountStore
import com.meshconnect.link.store.getAccountsFromPayload
import com.meshconnect.link.ui.LaunchLink
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
                logD("Payload received. $payload")
            }
        }

        // Create LinkConfiguration
        val configuration = LinkConfiguration(
            token = "linkToken"
        )

        // Launch Link
        binding.linkButton.setOnClickListener {
            linkLauncher.launch(configuration)
        }

        // Subscribe for accounts saved into secure storage
        lifecycleScope.launch(Dispatchers.IO) {
            accountStore.accounts().collect { accounts ->
                runOnUiThread {
                    binding.accountsText.text = toString(accounts)
                }
            }
        }
    }

    private val linkLauncher = registerForActivityResult(LaunchLink()) { result ->
        when (result) {
            is LinkSuccess -> handlePayloads(result.payloads)
            is LinkExit -> logD("Exit. ${result.errorMessage}")
        }
    }

    private fun handlePayloads(payloads: List<LinkPayload>) {
        payloads.forEach { payload ->
            when (payload) {
                is AccessTokenPayload -> {
                    logD("Broker connected. $payload")
                    // save accounts into secure storage (optional)
                    saveAccountsFromPayload(payload)
                }

                is TransferFinishedSuccessPayload -> {
                    logD("Transfer succeed. $payload")
                }

                is TransferFinishedErrorPayload -> {
                    logD("Transfer failed. $payload")
                }
            }
        }
    }

    private fun saveAccountsFromPayload(payload: AccessTokenPayload) {
        lifecycleScope.launch(Dispatchers.IO) {
            accountStore.insert(getAccountsFromPayload(payload))
        }
    }

    private fun logD(obj: Any?, TAG: String = "meshLog") {
        Log.d(TAG, obj.toString())
    }
}
