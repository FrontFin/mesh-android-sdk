package com.getfront.android

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import com.getfront.android.databinding.ConnectActivityBinding
import com.getfront.catalog.FrontCatalogContract
import com.getfront.catalog.store.createPreferenceAccountStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConnectActivity : AppCompatActivity() {

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ConnectActivityBinding.inflate(layoutInflater)
    }
    private val accountStore = createPreferenceAccountStore(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        /**
         * Listen for accounts change.
         */
        lifecycleScope.launch(Dispatchers.IO) {
            accountStore.accounts().collect { accounts ->
                runOnUiThread {
                    binding.accountsText.text = accounts.joinToString("\n") { account ->
                        """
                           <b>brokerName:</b> ${account.brokerName}<br>
                           <b>accountId:</b> ${account.accountId}<br>
                           <b>accessToken:</b> ${account.accessToken.take(n = 20)}...
                           <b>refreshToken:</b> ${account.refreshToken?.take(n = 20)}...
                        """
                    }.let { HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY) }
                }
            }
        }
        /**
         * Launch catalog.
         */
        binding.connectBtn.setOnClickListener {
            catalogResultLauncher.launch(
                "catalogLink"
            )
        }
    }

    /**
     * Receive connected accounts via ActivityResult.
     */
    private val catalogResultLauncher = registerForActivityResult(
        FrontCatalogContract()
    ) { accounts ->
        if (accounts != null) {
            /**
             * Save accounts to storage.
             */
            lifecycleScope.launch(Dispatchers.IO) {
                accountStore.insert(accounts)
            }
        }
        Log.i("FrontAccounts", accounts.toString())
    }
}
