package com.getfront.catalog.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import com.getfront.catalog.R
import com.getfront.catalog.databinding.BrokerCatalogActivityBinding
import com.getfront.catalog.entity.CatalogResponse
import com.getfront.catalog.entity.FrontAccount
import com.getfront.catalog.ui.web.BrokerWebChromeClient
import com.getfront.catalog.ui.web.FrontWebViewClient
import com.getfront.catalog.ui.web.JSBridge
import com.getfront.catalog.utils.getParcelableList
import com.getfront.catalog.utils.intent
import com.getfront.catalog.utils.lazyNone
import com.getfront.catalog.utils.observeEvent
import com.getfront.catalog.utils.onClick
import com.getfront.catalog.utils.showToast
import com.getfront.catalog.utils.viewBinding
import com.getfront.catalog.utils.viewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal class BrokerCatalogActivity : AppCompatActivity() {

    private val link get() = intent.getStringExtra(LINK)!!

    private val chromeClient by lazyNone { ChromeClient() }

    private val binding by viewBinding(BrokerCatalogActivityBinding::inflate)

    private val viewModel by viewModel<BrokerConnectViewModel>(
        BrokerConnectViewModel.Factory()
    )

    private var hasDialog = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.close.onClick { finish() }
        binding.back.onClick { onBackPressed() }

        observeCatalogResponse()
        observeThrowable()
        openWebView(link)
    }

    override fun onBackPressed() {
        binding.webView.apply {
            if (canGoBack()) goBack() else finish()
        }
    }

    private fun observeCatalogResponse() {
        observeEvent(viewModel.catalogResponse) {
            onCatalogResponse(it)
        }
    }

    private fun observeThrowable() {
        observeEvent(viewModel.throwable) {
            showMessage(it.message)
        }
    }

    private fun openWebView(url: String) {
        binding.webView.apply {
            @SuppressLint("SetJavaScriptEnabled")
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.setSupportMultipleWindows(true)
            addJavascriptInterface(JSBridge(viewModel), JSBridge.NAME)
            setBackgroundColor(Color.TRANSPARENT)
            webViewClient = FrontWebViewClient()
            webChromeClient = chromeClient
            loadUrl(url)
        }
    }

    private fun onCatalogResponse(response: CatalogResponse) = when (response) {
        is CatalogResponse.Connected -> onConnected(response)
        is CatalogResponse.Title -> onTitle(response)
        is CatalogResponse.Loaded -> Unit
        is CatalogResponse.Close -> finish()
        is CatalogResponse.Done -> finish()
    }

    private fun onConnected(connected: CatalogResponse.Connected) {
        val list = connected.accounts
        val arrayList = if (list is ArrayList<FrontAccount>) list else ArrayList(list)
        val data = Intent().apply { putParcelableArrayListExtra(DATA, arrayList) }
        setResult(RESULT_OK, data)
    }

    private fun onTitle(title: CatalogResponse.Title) {
        binding.title.text = title.title
        binding.toolbar.isGone = title.hideTitle == true
        binding.back.isGone = title.hideBackButton == true
    }

    private fun showMessage(message: String?) = when {
        message.isNullOrEmpty() -> Unit
        message.length <= MAX_TOAST_SIZE -> showToast(message)
        else -> showDialog {
            setMessage(message)
            setPositiveButton(R.string.okay, null)
            setCancelable(false)
            show()
        }
    }

    private fun showDialog(block: AlertDialog.Builder.() -> Unit) {
        if (hasDialog.not()) {
            hasDialog = true
            block(
                MaterialAlertDialogBuilder(this)
                    .setOnDismissListener {
                        hasDialog = false
                    }
            )
        }
    }

    inner class ChromeClient : BrokerWebChromeClient() {
        override fun launchWebView(url: String) {
            WebViewActivity.launch(
                this@BrokerCatalogActivity, url, binding.title.text.toString()
            )
        }
    }

    companion object {
        private const val DATA = "data"
        private const val LINK = "link"

        private const val MAX_TOAST_SIZE = 38

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
