package com.meshconnect.link.ui

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

/**
 * Implements [ActivityResultContract] to start catalog activity
 * and returns [LinkResult] in callback.
 * ```
 *  private val linkLauncher = registerForActivityResult(
 *      LinkTokenContract()
 *  ) { result ->
 *      when(result) {
 *          is LinkResult.Success -> {
 *              handlePayloads(result.payloads)
 *          }
 *          is LinkResult.Cancelled -> {
 *              // user cancelled the flow by clicking on back or close button
 *              // probably because of an error
 *              log("Cancelled ${result.error?.message}")
 *          }
 *      }
 *  }
 *
 *  linkButton.setOnClickListener {
 *      linkLauncher.launch("linkToken")
 *  }
 * ```
 */
@Suppress("unused")
class LinkContract : ActivityResultContract<String, LinkResult>() {

    /**
     * Returns intent to start catalog activity.
     */
    override fun createIntent(context: Context, input: String): Intent {
        return LinkActivity.getLinkIntent(context, input)
    }

    /**
     * Extracts [LinkResult] from result intent.
     */
    override fun parseResult(resultCode: Int, intent: Intent?): LinkResult {
        return LinkActivity.getLinkResult(intent)
    }
}
