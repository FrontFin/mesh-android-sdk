package com.meshconnect.link.ui

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

/**
 * Implements [ActivityResultContract] to start catalog activity
 * and returns [LinkResult] in callback.
 * ```
 *  private val linkLauncher = registerForActivityResult(LinkContract()) { result ->
 *      when(result) {
 *          is LinkSuccess -> {
 *              // success transaction
 *              Log.d("LinkResult", "Succeed. ${result.payloads}")
 *          }
 *          is LinkExit -> {
 *              // user exited the flow by clicking on back or close button
 *              // probably because of an error
 *              Log.d("LinkResult", "Exited. ${result.errorMessage}")
 *          }
 *      }
 *  }
 *
 *  linkButton.setOnClickListener {
 *      linkLauncher.launch("linkToken")
 *  }
 * ```
 */
@Deprecated(
    "'LaunchLink' is available",
    ReplaceWith("LaunchLink", "com.meshconnect.link.ui.LaunchLink"),
)
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
