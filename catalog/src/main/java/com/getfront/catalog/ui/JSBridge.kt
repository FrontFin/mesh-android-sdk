package com.getfront.catalog.ui

import android.webkit.JavascriptInterface
import com.getfront.catalog.utils.logD

internal class JSBridge(private val callback: Callback) {

    @JavascriptInterface
    fun sendNativeMessage(payloadJson: String) {
        logD("nativeMessage: $payloadJson")
        callback.onJsonReceived(payloadJson)
    }

    companion object {
        const val NAME = "JSBridge"
    }

    interface Callback {
        fun onJsonReceived(payloadJson: String)
    }
}
