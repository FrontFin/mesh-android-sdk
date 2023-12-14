package com.meshconnect.link.ui

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.meshconnect.link.entity.LinkConfiguration

/**
 * Implements [ActivityResultContract] to start the Link Activity
 * and returns [LinkResult] in callback.
 * ```
 *  // Register an Activity Result callback
 *  private val linkLauncher = registerForActivityResult(LaunchLink()) { result ->
 *      when(result) {
 *          is LinkSuccess -> /* handle success */
 *          is LinkExit -> /* handle exit */
 *      }
 *  }
 *
 *  // Create a LinkConfiguration
 *  val configuration = LinkConfiguration(
 *      token = "linkToken"
 *  )
 *
 *  // Launch Link
 *  linkLauncher.launch(configuration)
 * ```
 */
class LaunchLink : ActivityResultContract<LinkConfiguration, LinkResult>() {

    /**
     * Returns [Intent] to start the Link Activity.
     */
    override fun createIntent(context: Context, input: LinkConfiguration): Intent {
        return LinkActivity.getLinkIntent(context, input)
    }

    /**
     * Extracts [LinkResult] from [Intent].
     */
    override fun parseResult(resultCode: Int, intent: Intent?): LinkResult {
        return LinkActivity.getLinkResult(intent)
    }
}
