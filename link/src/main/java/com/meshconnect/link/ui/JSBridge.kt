package com.meshconnect.link.ui

import android.webkit.JavascriptInterface

internal class JSBridge(
    private val onJsonReceived: (String) -> Unit,
) {
    @JavascriptInterface
    fun sendNativeMessage(payloadJson: String) {
        onJsonReceived(payloadJson)
    }

    companion object {
        const val NAME = "JSBridge"

        // Link calls JSBridge.sendNativeMessage(json). A web message listener object
        // only has postMessage, so add sendNativeMessage on top of it.
        const val SEND_NATIVE_MESSAGE_SCRIPT =
            "if (window.$NAME) $NAME.sendNativeMessage = function (json) { $NAME.postMessage(json) };"
    }
}
