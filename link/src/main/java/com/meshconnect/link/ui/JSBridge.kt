package com.meshconnect.link.ui

import android.webkit.JavascriptInterface

internal class JSBridge(
    private val onJsonReceived: (String) -> Unit
) {
    @JavascriptInterface
    fun sendNativeMessage(payloadJson: String) {
        onJsonReceived(payloadJson)
    }

    companion object {
        const val NAME = "JSBridge"
    }
}
