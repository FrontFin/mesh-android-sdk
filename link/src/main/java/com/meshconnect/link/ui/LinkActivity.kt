package com.meshconnect.link.ui

import android.annotation.SuppressLint
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
import com.meshconnect.link.BuildConfig
import com.meshconnect.link.R
import com.meshconnect.link.databinding.LinkActivityBinding
import com.meshconnect.link.entity.LinkEvent
import com.meshconnect.link.utils.alertDialog
import com.meshconnect.link.utils.decodeCatching
import com.meshconnect.link.utils.getParcelableExtraCompat
import com.meshconnect.link.utils.intent
import com.meshconnect.link.utils.lazyNone
import com.meshconnect.link.utils.observeEvent
import com.meshconnect.link.utils.onClick
import com.meshconnect.link.utils.showToast
import com.meshconnect.link.utils.viewBinding
import com.meshconnect.link.utils.viewModel
import com.meshconnect.link.utils.windowInsetsController
import java.net.URL

internal class LinkActivity : AppCompatActivity() {

    companion object {
        private const val LINK = "link"
        private const val DATA = "data"
        private const val MAX_TOAST_MSG_LENGTH = 38

        fun getLinkIntent(activity: Context, catalogLink: String): Intent {
            return intent<LinkActivity>(activity).putExtra(LINK, catalogLink)
        }

        fun getLinkResult(data: Intent?): LinkResult {
            val result = data?.getParcelableExtraCompat<LinkResult>(DATA)
            return result ?: LinkResult.Exited()
        }
    }

    private val linkResult by lazyNone { decodeCatching(intent.getStringExtra(LINK)) }
    private val linkHost by lazyNone { URL(linkResult.getOrNull()).host }
    private val binding by viewBinding(LinkActivityBinding::inflate)
    private val viewModel by viewModel<LinkViewModel>(LinkViewModel.Factory())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val link = linkResult.getOrNull()
        if (link == null) {
            setResult(RESULT_CANCELED, LinkResult.Exited(linkResult.exceptionOrNull()))
            super.finish()
            return
        }

        setContentView(binding.root)

        windowInsetsController {
            isAppearanceLightNavigationBars = true
            isAppearanceLightStatusBars = true
        }

        binding.back.onClick { onBack() }
        binding.close.onClick { showCloseDialog() }
        binding.toolbar.isVisible = false

        observeLinkEvent()
        observeThrowable()
        openWebView(link)

        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
    }

    override fun onBackPressed() {
        onBack()
    }

    private fun onBack() {
        binding.webView.run {
            when {
                url?.endsWith("broker-connect/done") == true -> {
                    showToast(getString(R.string.back_not_allowed))
                }
                canGoBack() -> evaluateJavascript("window.history.go(-1)", null)
                else -> finish()
            }
        }
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

    private fun observeLinkEvent() {
        observeEvent(viewModel.linkEvent) { event ->
            when (event) {
                is LinkEvent.Close, LinkEvent.Done -> finish()
                is LinkEvent.ShowClose -> showCloseDialog()
                is LinkEvent.Payload, LinkEvent.Undefined -> Unit
            }
        }
    }

    override fun finish() {
        val payloads = viewModel.payloads
        if (payloads.isNotEmpty()) {
            setResult(RESULT_OK, LinkResult.Success(payloads))
        } else {
            setResult(RESULT_CANCELED, LinkResult.Exited(viewModel.error))
        }
        super.finish()
    }

    private fun setResult(resultCode: Int, result: LinkResult) {
        setResult(resultCode, Intent().apply { putExtra(DATA, result) })
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
            binding.toolbar.isGone = isLinkUrl(url)
        }

        private fun isLinkUrl(url: String?): Boolean {
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

        private val target
            get() = WebView(this@LinkActivity).apply {
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
}
