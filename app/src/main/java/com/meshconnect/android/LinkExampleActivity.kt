package com.meshconnect.android

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.meshconnect.android.databinding.LinkExampleActivityBinding
import com.meshconnect.link.LinkEvents
import com.meshconnect.link.LinkPayloads
import com.meshconnect.link.entity.AccessTokenPayload
import com.meshconnect.link.entity.DelayedAuthPayload
import com.meshconnect.link.entity.LinkConfiguration
import com.meshconnect.link.entity.LinkPayload
import com.meshconnect.link.entity.TransferFinishedErrorPayload
import com.meshconnect.link.entity.TransferFinishedSuccessPayload
import com.meshconnect.link.ui.LaunchLink
import com.meshconnect.link.ui.LinkExit
import com.meshconnect.link.ui.LinkSuccess
import kotlinx.coroutines.launch

class LinkExampleActivity : AppCompatActivity() {
    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        LinkExampleActivityBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Subscribe for payloads
        lifecycleScope.launch {
            LinkPayloads.collect {
                logD("Payload received. $it")
            }
        }

        // Subscribe for events
        lifecycleScope.launch {
            LinkEvents.collect {
                logD("Event received. $it")
            }
        }

        // Create LinkConfiguration and launch the Link
        binding.linkBtn.setOnClickListener {
            val token = binding.linkTokenInputText.text.toString()
            if (token.isNotEmpty()) {
                binding.linkTokenInputText.text = null
                linkLauncher.launch(LinkConfiguration(token))
            }
        }

        binding.linkTokenInputText.requestFocus()
    }

    // Register an Activity Result callback
    private val linkLauncher =
        registerForActivityResult(LaunchLink()) { result ->
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
                }

                is DelayedAuthPayload -> {
                    logD("Delayed authentication. $payload")
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

    private fun logD(obj: Any?) {
        Log.d("meshLog", obj.toString())
    }
}
