package com.getfront.catalog.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.getfront.catalog.R
import com.getfront.catalog.databinding.BrokerCatalogActivityBinding
import com.getfront.catalog.entity.CatalogEvent
import com.getfront.catalog.entity.FrontAccount
import com.getfront.catalog.utils.alertDialog
import com.getfront.catalog.utils.getParcelableList
import com.getfront.catalog.utils.intent
import com.getfront.catalog.utils.lazyNone
import com.getfront.catalog.utils.observeEvent
import com.getfront.catalog.utils.onClick
import com.getfront.catalog.utils.showToast
import com.getfront.catalog.utils.viewBinding
import com.getfront.catalog.utils.viewModel
import com.getfront.catalog.utils.windowInsetsController
import java.net.URL

internal class BrokerCatalogActivity : AppCompatActivity() {

    private val link get() = intent.getStringExtra(LINK)!!
    private val linkHost by lazyNone { URL(link).host }
    private val binding by viewBinding(BrokerCatalogActivityBinding::inflate)
    private val viewModel by viewModel<BrokerConnectViewModel>(BrokerConnectViewModel.Factory())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        windowInsetsController {
            isAppearanceLightNavigationBars = true
            isAppearanceLightStatusBars = true
        }

        binding.back.onClick { onBack() }
        binding.close.onClick { showCloseDialog() }
        binding.toolbar.isVisible = false

        observeCatalogEvent()
        observeThrowable()
        openWebView(link)
    }

    override fun onBackPressed() {
        onBack()
    }

    private fun onBack() {
        binding.webView.evaluateJavascript("window.history.go(-1)", null)
    }

    private fun showCloseDialog() {
        alertDialog {
            setTitle(R.string.onCloseDialog_title)
            setMessage(R.string.onCloseDialog_message)
            setPositiveButton(R.string.onCloseDialog_exit) { _, _ -> finish() }
            setNegativeButton(R.string.onCloseDialog_cancel, null)
            setCancelable(false)
            show()
        }
    }

    private fun observeCatalogEvent() {
        observeEvent(viewModel.catalogEvent) { event ->
            when (event) {
                is CatalogEvent.Connected -> onConnected(event)
                is CatalogEvent.Close, CatalogEvent.Done -> finish()
                is CatalogEvent.ShowClose -> showCloseDialog()
                is CatalogEvent.Undefined -> Unit
            }
        }
    }

    private fun onConnected(connected: CatalogEvent.Connected) {
        val list = connected.accounts
        val arrayList = if (list is ArrayList<FrontAccount>) list else ArrayList(list)
        val data = Intent().apply { putParcelableArrayListExtra(DATA, arrayList) }
        setResult(RESULT_OK, data)
    }

    private fun observeThrowable() {
        observeEvent(viewModel.throwable) {
            showMessage(it.message)
        }
    }

    private fun showMessage(message: String?) {
        when {
            message.isNullOrEmpty() -> Unit
            message.length <= MAX_TOAST_MSG_LENGTH -> showToast(message)
            else -> alertDialog {
                setMessage(message)
                setPositiveButton(R.string.okay, null)
                setCancelable(false)
                show()
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun openWebView(url: String) {
        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.setSupportMultipleWindows(true)
            addJavascriptInterface(JSBridge(viewModel), JSBridge.NAME)
            setBackgroundColor(Color.TRANSPARENT)
            webViewClient = WebClient()
            webChromeClient = ChromeClient()
            loadUrl(url)
        }
    }

    inner class WebClient : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            binding.progress.isVisible = false
        }

        override fun onPageCommitVisible(view: WebView?, url: String?) {
            super.onPageCommitVisible(view, url)
            binding.toolbar.isGone = isFrontUrl(url)
        }

        private fun isFrontUrl(url: String?): Boolean {
            return try {
                URL(url).host == linkHost
            } catch (expected: Exception) {
                false
            }
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return false
        }
    }

    inner class ChromeClient : WebChromeClient() {

        private val target = WebView(this@BrokerCatalogActivity).apply {
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    request?.url?.let { actionView(it) }
                    return super.shouldOverrideUrlLoading(view, request)
                }
            }
        }

        override fun onCreateWindow(
            view: WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message?
        ): Boolean {
            val url = view?.hitTestResult?.extra

            return when {
                !url.isNullOrBlank() -> {
                    actionView(Uri.parse(url))
                    false
                }
                resultMsg != null -> {
                    (resultMsg.obj as WebView.WebViewTransport).webView = target
                    resultMsg.sendToTarget()
                    true
                }
                else -> false
            }
        }
    }

    private fun actionView(uri: Uri) {
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    companion object {
        private const val DATA = "data"
        private const val LINK = "link"

        private const val MAX_TOAST_MSG_LENGTH = 38

        fun getIntent(activity: Context, catalogLink: String) =
            intent<BrokerCatalogActivity>(activity)
                .putExtra(LINK, catalogLink)

        fun getAccounts(resultCode: Int, data: Intent?): List<FrontAccount>? {
            return if (resultCode == Activity.RESULT_OK && data != null) {
                getParcelableList(data, DATA)
            } else null
        }
    }
}
