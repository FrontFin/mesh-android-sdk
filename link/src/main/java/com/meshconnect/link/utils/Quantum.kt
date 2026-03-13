package com.meshconnect.link.utils

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView

internal object Quantum {
    fun newInstance(context: Context) = QuantumPlaceholder()
}

internal interface IQuantum {
    suspend fun initialize(
        atomicToken: String,
        webView: WebView,
        container: ViewGroup,
    )

    suspend fun goto(link: String)
}

class QuantumPlaceholder : IQuantum {
    override suspend fun initialize(
        atomicToken: String,
        webView: WebView,
        container: ViewGroup,
    ) {
        // no-op
    }

    override suspend fun goto(link: String) {
        // no-op
    }
}
