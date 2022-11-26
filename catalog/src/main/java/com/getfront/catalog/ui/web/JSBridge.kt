package com.getfront.catalog.ui.web

import android.webkit.JavascriptInterface

internal class JSBridge(private val callback: Callback) {

    @JavascriptInterface
    fun sendNativeMessage(payloadJson: String) {
        callback.onJsonReceived(payloadJson)
    }

    companion object {
        const val NAME = "JSBridge"
    }

    interface Callback {
        fun onJsonReceived(payloadJson: String)
    }
}
