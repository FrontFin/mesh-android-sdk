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
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
import com.meshconnect.link.utils.THEME_DARK
import com.meshconnect.link.utils.alertDialog
import com.meshconnect.link.utils.createURL
import com.meshconnect.link.utils.decodeToken
import com.meshconnect.link.utils.getOnLoadedScript
import com.meshconnect.link.utils.getParcelable
import com.meshconnect.link.utils.getThemeFromUrl
import com.meshconnect.link.utils.getThemeName
import com.meshconnect.link.utils.intent
import com.meshconnect.link.utils.isSystemThemeDark
import com.meshconnect.link.utils.isUrlWhitelisted
import com.meshconnect.link.utils.observeEvent
import com.meshconnect.link.utils.openTrueAuth
import com.meshconnect.link.utils.resolveLanguage
import com.meshconnect.link.utils.resolveTheme
import com.meshconnect.link.utils.showToast
import com.meshconnect.link.utils.viewBinding
import com.meshconnect.link.utils.viewModel
import com.meshconnect.link.utils.windowInsetsController
import java.net.URL

internal class LinkActivity : AppCompatActivity() {
    companion object {
        private const val TOKEN = "token"
        private const val ACCESS_TOKENS = "access_tokens"
        private const val DISABLE_WHITELIST = "disable_whitelist"
        private const val DATA = "data"
        private const val LANGUAGE = "language"
        private const val FIAT_CURRENCY = "fiat_currency"
        private const val THEME = "theme"
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
                    .putExtra(FIAT_CURRENCY, config.displayFiatCurrency)
                    .putExtra(THEME, getThemeName(config.theme))

            val accessTokens = config.accessTokens

            if (!accessTokens.isNullOrEmpty()) {
                intent.putExtra(ACCESS_TOKENS, Gson().toJson(accessTokens))
            }
            return intent
        }

        fun getLinkResult(data: Intent?): LinkResult {
            return try {
                data?.getParcelable<LinkResult>(DATA) ?: LinkExit()
            } catch (expected: Exception) {
                LinkExit(expected.message)
            }
        }
    }

    private val binding by viewBinding(LinkActivityBinding::inflate)
    private val viewModel by viewModel<LinkViewModel>(LinkViewModel.Factory())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runCatching {
            val link = decodeToken(intent.getStringExtra(TOKEN))
            val language = resolveLanguage(intent.getStringExtra(LANGUAGE))
            val fiatCurrency = intent.getStringExtra(FIAT_CURRENCY)
            val theme = resolveTheme(intent.getStringExtra(THEME), this::isSystemThemeDark)
            createURL(link, mapOf("lng" to language, "fiatCur" to fiatCurrency, "th" to theme, "platform" to "android"))
        }.onSuccess { url ->
            setContentView(binding.root)

            applyThemeFromUrl(url.toString())

            binding.back.setOnClickListener { onBack() }
            binding.close.setOnClickListener { showCloseDialog() }
            binding.toolbar.isVisible = false

            observeLinkEvent()
            observeThrowable()
            openWebView(url, savedInstanceState)

            WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        }.onFailure { throwable ->
            Log.e("MeshSDK", "Failed to initialize SDK", throwable)
            setExitResult(throwable)
            super.finish()
        }
    }

    private fun applyThemeFromUrl(url: String) {
        val theme = getThemeFromUrl(url)
        val isDarkTheme = resolveTheme(theme, this::isSystemThemeDark) == THEME_DARK

        val topColor = getColor(if (isDarkTheme) R.color.coldGray70 else R.color.coldGray0)
        val bottomColor = getColor(if (isDarkTheme) R.color.gray80 else R.color.gray0)

        findViewById<ViewGroup>(android.R.id.content).setBackgroundColor(bottomColor)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = topColor
        window.navigationBarColor = bottomColor

        windowInsetsController {
            isAppearanceLightNavigationBars = !isDarkTheme
            isAppearanceLightStatusBars = !isDarkTheme
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
        binding.webView.resumeTimers()
    }

    override fun onPause() {
        super.onPause()
        binding.webView.onPause()
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
                is LinkEvent.TrueAuth -> {
                    /** To re-enable the TrueAuth replace with [onTrueAuthEvent] **/
                    showToast(R.string.integration_disabled)
                }
            }
        }
    }

    @Suppress("UnusedPrivateMember")
    private fun onTrueAuthEvent(event: LinkEvent.TrueAuth) {
        openTrueAuth(
            lifecycleScope = lifecycleScope,
            webView = binding.webView,
            webViewContainer = binding.webViewContainer,
            event = event,
            onError = { showToast(R.string.not_able_to_perform) },
        )
    }

    private fun onLinkLoaded() {
        val script =
            getOnLoadedScript(
                version = BuildConfig.VERSION,
                accessTokens = intent.getStringExtra(ACCESS_TOKENS),
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
        setResult(RESULT_CANCELED, LinkExit(throwable?.message))
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

    /**
     * The WebView's own history and page state, so a recreated Activity resumes
     * the flow instead of starting a new one.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.webView.saveState(outState)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun openWebView(
        url: URL,
        savedInstanceState: Bundle?,
    ) {
        val disableWhiteList = intent.getBooleanExtra(DISABLE_WHITELIST, false)

        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.setSupportMultipleWindows(true)
            // Without this, WebView silently drops any window.open() that isn't
            // inside a live user gesture (e.g. an auto-open fired from an async
            // event) and never even calls onCreateWindow below - so wallet
            // deep links and OAuth handoffs that should open automatically just
            // dead-end instead.
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            addJavascriptInterface(JSBridge { viewModel.onJsonReceived(it) }, JSBridge.NAME)
            setBackgroundColor(Color.TRANSPARENT)
            webViewClient = WebClient(disableWhiteList, linkHost = url.host)
            webChromeClient = ChromeClient()

            // Restore rather than reload where possible. The Activity is
            // recreated whenever the system reclaims it while the user is away
            // in a wallet or a browser, and reloading re-opens the URL with the
            // same token. An MFS exchange code is single-use, so that lands on
            // Link's "session has expired" page and the transfer in progress is
            // lost. restoreState returns null when there is nothing to restore,
            // which is the first launch.
            if (savedInstanceState == null || restoreState(savedInstanceState) == null) {
                loadUrl(url.toString())
            }
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
            val url = request?.url ?: return true
            val allowInWebView =
                when {
                    disableWhiteList -> url.scheme.equals("https", ignoreCase = true)
                    else -> isUrlWhitelisted(url.toString(), url.host.orEmpty())
                }
            if (!allowInWebView && request?.isForMainFrame == true) {
                // Not something we render ourselves (an exchange/OAuth page) -
                // hand it to Android like any wallet deep link, instead of
                // silently dropping it. Custom schemes (wallet://) never match
                // the whitelist either, so this is also how those get opened.
                // Subframe requests (e.g. an embedded iframe) are only ever
                // rejected here, never externalized - launching an app over a
                // blocked iframe would hijack the whole task unexpectedly.
                actionView(url)
            }
            return !allowInWebView // return 'true' to reject loading the url
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
                                // This popup is never actually rendered - every
                                // navigation attempt is rejected below - so
                                // skipping redirect hops here (as opposed to the
                                // original navigation) doesn't defer to a later
                                // call, it just drops that hop entirely. Wallet
                                // connect popups commonly redirect from an https
                                // bridge to the wallet's custom-scheme deep link,
                                // so the real target is often the redirect.
                                request?.let { actionView(it.url) }
                                return true // return 'true' to reject loading the url
                            }
                        }
                }

        override fun onCreateWindow(
            view: WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message?,
        ): Boolean {
            if (resultMsg != null) {
                (resultMsg.obj as WebView.WebViewTransport).webView = target
                resultMsg.sendToTarget()
                return true
            }
            return false
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
        // Without FLAG_ACTIVITY_NEW_TASK, Android ignores the target app's own
        // taskAffinity and pushes its activity onto THIS task's back stack
        // instead of switching to its own task — it renders on top of the host
        // app with no clean way back, rather than as a separate app/task.
        val intent =
            Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        startActivity(intent)
    }
}
