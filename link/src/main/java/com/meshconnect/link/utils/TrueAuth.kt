package com.meshconnect.link.utils

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleCoroutineScope
import com.meshconnect.link.R
import com.meshconnect.link.entity.LinkEvent
import com.meshconnect.link.ui.JSBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal fun handleTrueAuth(
    lifecycleScope: LifecycleCoroutineScope,
    webView: WebView,
    webViewContainer: ViewGroup,
    event: LinkEvent.TrueAuth,
    onShowToast: (Int) -> Unit,
) {
    val trueAuthWebView = WebView(webViewContainer.context)

    fun onTrueAuthEvent(json: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                runCatching { extractTrueAuthResult(json) }
            }.onSuccess {
                webView.evaluateJavascript("window.trueAuthResult='$it'", null)
                // test WebView destroy before uncomment
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
            Quantum(webViewContainer.context).apply {
                initialize(event.atomicToken, trueAuthWebView, webViewContainer)
                goto(event.link)
            }
            webViewContainer.isVisible = true
        } catch (expected: Exception) {
            trueAuthWebView.destroy()
            webViewContainer.removeAllViews()
            webViewContainer.isVisible = false
            onShowToast(R.string.not_able_to_perform)
        }
    }
}

/**
 * This is a placeholder class for Quantum.
 * To re-enable it, follow the next steps:
 *  1. Add a dependency 'implementation libs.atomic.quantum' to link/build.gradle
 *  2. Replace with: financial.atomic.quantum.Quantum(context)
 */
@Suppress("UnusedPrivateMember")
internal class Quantum(private val context: Context) {
    private fun throwPlaceholderError() {
        error("Placeholder used. Enable the True Auth.")
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
