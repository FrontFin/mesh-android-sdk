package com.getfront.catalog.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.getfront.catalog.databinding.WebviewActivityBinding
import com.getfront.catalog.ui.web.FrontWebViewClient
import com.getfront.catalog.utils.ifNotBlank
import com.getfront.catalog.utils.intent
import com.getfront.catalog.utils.viewBinding

internal class WebViewActivity : AppCompatActivity() {

    private val title get() = intent.getStringExtra(TITLE)
    private val targetUrl get() = intent.getStringExtra(URL)

    private val binding by viewBinding(WebviewActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.tvSkip.setOnClickListener { finish() }
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        title.ifNotBlank { binding.tvTitle.text = it }

        setupWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = FrontWebViewClient()
            setBackgroundColor(Color.TRANSPARENT)
            targetUrl.ifNotBlank { loadUrl(it) }
        }
    }

    override fun onBackPressed() {
        binding.webView.apply {
            if (canGoBack()) goBack() else super.onBackPressed()
        }
    }

    companion object {

        private const val URL = "URL"
        private const val TITLE = "TITLE"

        fun launch(context: Context, url: String, title: String? = null) {
            val intent = intent<WebViewActivity>(context)
                .putExtra(URL, url)
                .putExtra(TITLE, title)

            context.startActivity(intent)
        }
    }
}
