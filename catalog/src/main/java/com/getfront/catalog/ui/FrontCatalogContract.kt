package com.getfront.catalog.ui

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

/**
 * Implements [ActivityResultContract] to start catalog activity
 * and returns [FrontCatalogResult] in callback.
 * ```
 *  private val catalogLauncher = registerForActivityResult(
 *      FrontCatalogContract()
 *  ) { result ->
 *      when(result) {
 *          is FrontCatalogResult.Success -> {
 *              handlePayloads(result.payloads)
 *          }
 *          is FrontCatalogResult.Cancelled -> {
 *              // user cancelled the flow by clicking on back or close button
 *              // probably because of an error
 *              log("Cancelled ${result.error?.message}")
 *          }
 *      }
 *  }
 *
 *  connectBtn.setOnClickListener {
 *      catalogLauncher.launch("catalogLink")
 *  }
 * ```
 */
@Suppress("unused")
class FrontCatalogContract : ActivityResultContract<String, FrontCatalogResult>() {

    /**
     * Returns intent to start catalog activity.
     */
    override fun createIntent(context: Context, input: String): Intent {
        return getFrontCatalogIntent(context, input)
    }

    /**
     * Extracts [FrontCatalogResult] from result intent.
     */
    override fun parseResult(resultCode: Int, intent: Intent?): FrontCatalogResult {
        return getFrontCatalogResult(intent)
    }
}
