package com.meshconnect.link.utils

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView

internal object Quantum {
    fun newInstance(expected: Context): IQuantum = QuantumPlaceholder()
    // Replace with: financial.atomic.quantum.Quantum(context) when re-enabling
    // By adding 'implementation libs.atomic.quantum' to link/build.gradle
}

internal interface IQuantum {
    suspend fun initialize(
        atomicToken: String,
        webView: WebView,
        container: ViewGroup,
    )

    suspend fun goto(link: String)
}

internal class QuantumPlaceholder : IQuantum {
    override suspend fun initialize(
        atomicToken: String,
        webView: WebView,
        container: ViewGroup,
    ) = Unit

    override suspend fun goto(link: String) = Unit
}
