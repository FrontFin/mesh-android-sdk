package com.getfront.catalog.ui.web

import android.os.Message
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.getfront.catalog.utils.ifNotBlank

internal abstract class BrokerWebChromeClient : WebChromeClient() {

    abstract fun launchWebView(url: String)

    override fun onCreateWindow(
        view: WebView,
        dialog: Boolean,
        userGesture: Boolean,
        resultMsg: Message
    ): Boolean {
        val data = view.hitTestResult.extra
        data.ifNotBlank { launchWebView(it) }

        return false
    }
}
