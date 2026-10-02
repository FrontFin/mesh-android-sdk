package com.meshconnect.android

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.meshconnect.android.databinding.LinkExampleActivityBinding
import com.meshconnect.link.LinkEvents
import com.meshconnect.link.LinkPayloads
import com.meshconnect.link.entity.AccessTokenPayload
import com.meshconnect.link.entity.DelayedAuthPayload
import com.meshconnect.link.entity.LinkConfiguration
import com.meshconnect.link.entity.LinkPayload
import com.meshconnect.link.entity.MeshLinkEnvironment
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
        WebView.setWebContentsDebuggingEnabled(true)
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
            val token = binding.linkTokenInputText.text.toString().trim()
            if (token.isNotEmpty()) {
                binding.linkTokenInputText.text = null
                linkLauncher.launch(configurationFor(token))
            }
        }

        // selectionRequired keeps one checked once the user picks, but does not
        // check one to begin with.
        binding.environmentGroup.check(R.id.envProd)
        binding.linkTokenInputText.requestFocus()
    }

    /**
     * The field takes either kind of token. A link token is base64 of a URL (or
     * the URL itself) and carries its own host; an MFS session token is a bare
     * string that does not, so the picker supplies one.
     *
     * Detected by decoding rather than by prefix, so it does not depend on how
     * MFS happens to name its tokens.
     */
    private fun configurationFor(token: String): LinkConfiguration =
        if (isLinkToken(token)) {
            LinkConfiguration(token, language = "en")
        } else {
            LinkConfiguration(
                token =
                    LinkConfiguration.linkToken(
                        sessionToken = token,
                        environment = selectedEnvironment(),
                    ),
                language = "en",
            )
        }

    private fun isLinkToken(token: String): Boolean {
        if (token.startsWith("http://") || token.startsWith("https://")) return true
        return runCatching {
            val decoded = String(Base64.decode(token, Base64.DEFAULT))
            decoded.startsWith("http://") || decoded.startsWith("https://")
        }.getOrDefault(false)
    }

    private fun selectedEnvironment(): MeshLinkEnvironment =
        when (binding.environmentGroup.checkedButtonId) {
            R.id.envSbx -> MeshLinkEnvironment.SBX
            R.id.envDev -> MeshLinkEnvironment.DEV
            else -> MeshLinkEnvironment.PROD
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
