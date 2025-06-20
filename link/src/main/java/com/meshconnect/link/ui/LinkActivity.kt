package com.meshconnect.link.ui

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.meshconnect.link.BuildConfig
import com.meshconnect.link.R
import com.meshconnect.link.databinding.LinkActivityBinding
import com.meshconnect.link.entity.LinkConfiguration
import com.meshconnect.link.entity.LinkEvent
import com.meshconnect.link.entity.LinkPayload
import com.meshconnect.link.utils.alertDialog
import com.meshconnect.link.utils.createURL
import com.meshconnect.link.utils.decodeToken
import com.meshconnect.link.utils.extractTrueAuthResult
import com.meshconnect.link.utils.getLinkStyleFromLinkUrl
import com.meshconnect.link.utils.getOnLoadedScript
import com.meshconnect.link.utils.getParcelable
import com.meshconnect.link.utils.intent
import com.meshconnect.link.utils.isSystemDarkTheme
import com.meshconnect.link.utils.isUrlWhitelisted
import com.meshconnect.link.utils.observeEvent
import com.meshconnect.link.utils.showToast
import com.meshconnect.link.utils.viewBinding
import com.meshconnect.link.utils.viewModel
import com.meshconnect.link.utils.windowInsetsController
import financial.atomic.quantum.Quantum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

internal class LinkActivity : AppCompatActivity() {
    companion object {
        private const val TOKEN = "token"
        private const val ACCESS_TOKENS = "access_tokens"
        private const val TRANSFER_TOKENS = "transfer_tokens"
        private const val DISABLE_WHITELIST = "disable_whitelist"
        private const val DATA = "data"
        private const val LANGUAGE = "language"
        private const val MAX_TOAST_MSG_LENGTH = 38
        private const val CBW_HOST = "wallet.coinbase.com"
        private const val CBW_PACKAGE_NAME = "org.toshi"

        fun getLinkIntent(
            activity: Context,
            config: LinkConfiguration,
        ): Intent {
            val intent =
                intent<LinkActivity>(activity)
                    .putExtra(TOKEN, config.token)
                    .putExtra(DISABLE_WHITELIST, config.disableDomainWhiteList)
                    .putExtra(LANGUAGE, config.language)

            val accessTokens = config.accessTokens
            val transferTokens = config.transferDestinationTokens

            if (!accessTokens.isNullOrEmpty() || !transferTokens.isNullOrEmpty()) {
                val gson = Gson()
                if (accessTokens != null) {
                    intent.putExtra(ACCESS_TOKENS, gson.toJson(accessTokens))
                }
                if (transferTokens != null) {
                    intent.putExtra(TRANSFER_TOKENS, gson.toJson(transferTokens))
                }
            }
            return intent
        }

        fun getLinkResult(data: Intent?): LinkResult {
            val result = data?.getParcelable<LinkResult>(DATA)
            return result ?: LinkExit()
        }
    }

    private val binding by viewBinding(LinkActivityBinding::inflate)
    private val viewModel by viewModel<LinkViewModel>(LinkViewModel.Factory())
    private val quantum by lazy { Quantum(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runCatching {
            val link = decodeToken(intent.getStringExtra(TOKEN))
            createURL(link, mapOf("lng" to intent.getStringExtra(LANGUAGE)))
        }.onSuccess { url ->
            setContentView(binding.root)

            applyTheme(url.toString())

            binding.back.setOnClickListener { onBack() }
            binding.close.setOnClickListener { showCloseDialog() }
            binding.toolbar.isVisible = false

            observeLinkEvent()
            observeThrowable()
            openWebView(url)

            WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        }.onFailure { throwable ->
            Log.e("MeshSDK", "Failed to initialize SDK", throwable)
            setExitResult(throwable)
            super.finish()
        }
    }

    private fun applyTheme(url: String) {
        val linkStyle = getLinkStyleFromLinkUrl(url)
        val isDarkTheme =
            when (linkStyle?.th) {
                "dark" -> true
                "system" -> isSystemDarkTheme()
                else -> false
            }

        val topColor =
            when {
                isDarkTheme -> getColor(R.color.coldGray70)
                else -> getColor(R.color.coldGray0)
            }

        val bottomColor =
            when {
                isDarkTheme -> getColor(R.color.gray80)
                else -> getColor(R.color.gray0)
            }

        findViewById<ViewGroup>(android.R.id.content).setBackgroundColor(bottomColor)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = topColor
        window.navigationBarColor = bottomColor

        windowInsetsController {
            isAppearanceLightNavigationBars = !isDarkTheme
            isAppearanceLightStatusBars = !isDarkTheme
        }
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        onBack()
    }

    private fun onBack() {
        if (binding.webViewContainer.isVisible) {
            binding.webViewContainer.removeAllViews()
            binding.webViewContainer.visibility = ViewGroup.GONE
        } else {
            binding.webView.run {
                when {
                    canGoBack() -> evaluateJavascript("window.history.go(-1)", null)
                    else -> finish()
                }
            }
        }
    }

    private fun showCloseDialog() {
        alertDialog {
            setTitle(R.string.exitDialog_title)
            setMessage(R.string.exitDialog_message)
            setPositiveButton(R.string.exitDialog_exit) { _, _ -> finish() }
            setNegativeButton(R.string.exitDialog_cancel, null)
            setCancelable(false)
            show()
        }
    }

    private fun observeLinkEvent() {
        observeEvent(viewModel.linkEvent) { event ->
            when (event) {
                is LinkEvent.Close, LinkEvent.Done -> finish()
                is LinkEvent.ShowClose -> showCloseDialog()
                is LinkEvent.Loaded -> onLinkLoaded()
                is LinkEvent.Payload -> Unit
                is LinkEvent.TrueAuth -> openTrueAuth(event.link)
            }
        }
    }

    private fun onLinkLoaded() {
        val script =
            getOnLoadedScript(
                version = BuildConfig.VERSION,
                accessTokens = intent.getStringExtra(ACCESS_TOKENS),
                transferDestinationTokens = intent.getStringExtra(TRANSFER_TOKENS),
            )
        binding.webView.evaluateJavascript(script, null)
    }

    override fun finish() {
        val payloads = viewModel.payloads
        if (payloads.isNotEmpty()) {
            setSuccessResult(payloads)
        } else {
            setExitResult(viewModel.error)
        }
        super.finish()
    }

    private fun setSuccessResult(payloads: List<LinkPayload>) {
        setResult(RESULT_OK, LinkSuccess(payloads))
    }

    private fun setExitResult(throwable: Throwable?) {
        setResult(RESULT_CANCELED, LinkExit(throwable))
    }

    private fun setResult(
        resultCode: Int,
        result: LinkResult,
    ) {
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
            else ->
                alertDialog {
                    setMessage(message)
                    setPositiveButton(android.R.string.ok, null)
                    setCancelable(false)
                    show()
                }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun openWebView(url: URL) {
        val disableWhiteList = intent.getBooleanExtra(DISABLE_WHITELIST, false)

        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.setSupportMultipleWindows(true)
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            addJavascriptInterface(JSBridge { viewModel.onJsonReceived(it) }, JSBridge.NAME)
            setBackgroundColor(Color.TRANSPARENT)
            webViewClient = WebClient(disableWhiteList, linkHost = url.host)
            webChromeClient = ChromeClient()
            loadUrl(url.toString())
        }
    }

    inner class WebClient(
        private val disableWhiteList: Boolean,
        private val linkHost: String,
    ) : WebViewClient() {
        override fun onPageCommitVisible(
            view: WebView?,
            url: String?,
        ) {
            super.onPageCommitVisible(view, url)
            binding.toolbar.isGone = isLinkUrl(url)
            binding.progress.isVisible = false
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
            request: WebResourceRequest?,
        ): Boolean {
            val override =
                when {
                    request == null -> true
                    disableWhiteList -> request.url.scheme != "https"
                    else -> !isUrlWhitelisted(request.url.toString(), request.url.host.orEmpty())
                }
            // return 'true' to reject loading the url by WebView
            return override
        }
    }

    inner class ChromeClient : WebChromeClient() {
        private val target
            get() =
                WebView(this@LinkActivity).apply {
                    webViewClient =
                        object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?,
                            ): Boolean {
                                if (request != null && !request.isRedirect) {
                                    actionView(request.url)
                                }
                                // return 'true' to reject loading the url by WebView
                                return true
                            }
                        }
                }

        override fun onCreateWindow(
            view: WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message?,
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

    @SuppressLint("SetJavaScriptEnabled")
    private val coinbaseLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result?.data?.data?.let { uri ->
                binding.webView2.apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.cacheMode = WebSettings.LOAD_NO_CACHE
                    loadUrl(uri.toString())
                }
            }
        }

    private fun onTrueAuthEvent(json: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                runCatching { extractTrueAuthResult(json) }
            }.onSuccess {
                binding.webView.evaluateJavascript("window.trueAuthResult='$it'", null)
            }
        }
    }

    private fun openTrueAuth(url: String) {
        val webView =
            WebView(this@LinkActivity).apply {
                setBackgroundColor(Color.TRANSPARENT)
                addJavascriptInterface(JSBridge(::onTrueAuthEvent), JSBridge.NAME)
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
            }
        binding.webViewContainer.removeAllViews()
        binding.webViewContainer.addView(webView)
        CookieManager.getInstance().removeSessionCookies(null)
        quantum.initialize(webView, binding.webViewContainer)
        lifecycleScope.launch {
            quantum.goto(url)
            binding.webViewContainer.visibility = ViewGroup.VISIBLE
        }
    }

    private fun actionView(uri: Uri) {
        try {
            if (uri.host == CBW_HOST) {
                val intent = packageManager.getLaunchIntentForPackage(CBW_PACKAGE_NAME)
                if (intent != null) {
                    intent.type = Intent.ACTION_VIEW
                    intent.flags = intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK.inv()
                    intent.data = uri
                    coinbaseLauncher.launch(intent)
                } else {
                    startViewIntent(uri)
                }
            } else {
                startViewIntent(uri)
            }
        } catch (expected: ActivityNotFoundException) {
            showToast(R.string.not_able_to_perform)
        }
    }

    private fun startViewIntent(uri: Uri) {
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
