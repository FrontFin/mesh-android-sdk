package com.meshconnect.link.utils

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleCoroutineScope
import com.meshconnect.link.entity.LinkEvent
import com.meshconnect.link.ui.JSBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Think about modification to class
// with a `destroy` function or subscription to Lifecycle.DESTROY event
// to release the resources from created views and Quantum instance

internal fun openTrueAuth(
    lifecycleScope: LifecycleCoroutineScope,
    webView: WebView,
    webViewContainer: ViewGroup,
    event: LinkEvent.TrueAuth,
    onError: (Exception) -> Unit,
) {
    val trueAuthWebView = WebView(webViewContainer.context)

    fun onTrueAuthEvent(json: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                runCatching { extractTrueAuthResult(json) }
            }.onSuccess {
                // Replace with `onTrueAuthSuccess` callback
                // to avoid a `webView` dependency in this function
                webView.evaluateJavascript("window.trueAuthResult='$it'", null)

                // Destroy the trueAuthWebView after success
                // trueAuthWebView.destroy()
            }
        }
    }

    trueAuthWebView.apply {
        setBackgroundColor(Color.TRANSPARENT)
        addJavascriptInterface(JSBridge(::onTrueAuthEvent), JSBridge.NAME)
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
    }

    webViewContainer.apply {
        removeAllViews()
        addView(trueAuthWebView)
    }

    CookieManager.getInstance().removeSessionCookies(null)

    lifecycleScope.launch {
        try {
            val quantum = QuantumPlaceholder(webViewContainer.context)
            quantum.initialize(event.atomicToken, trueAuthWebView, webViewContainer)
            quantum.goto(event.link)

            webViewContainer.isVisible = true
        } catch (expected: Exception) {
            trueAuthWebView.destroy()
            webViewContainer.removeAllViews()
            webViewContainer.isVisible = false
            onError(expected)
        }
    }
}

/**
 * This is a placeholder class for Quantum.
 * To re-enable it:
 *  1. Add a dependency 'implementation libs.atomic.quantum' to link/build.gradle
 *  2. Import `financial.atomic.quantum.Quantum`
 *  3. Remove QuantumPlaceholder class
 */
@Suppress("UnusedPrivateMember")
private class QuantumPlaceholder(private val context: Context) {
    private fun throwPlaceholderError() {
        error("Quantum is not enabled")
    }

    fun initialize(
        atomicToken: String,
        webView: WebView,
        container: ViewGroup,
    ) {
        throwPlaceholderError()
    }

    fun goto(link: String) {
        throwPlaceholderError()
    }
}
