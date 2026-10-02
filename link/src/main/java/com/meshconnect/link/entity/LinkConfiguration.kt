package com.meshconnect.link.entity

import com.meshconnect.link.utils.sessionLinkToken

/**
 * Configuration for launching the Mesh Connect Link UI.
 */
data class LinkConfiguration(
    /**
     * Link token returned by the Mesh backend.
     */
    val token: String,
    /**
     * Previously obtained [IntegrationAccessToken]s to pre-populate the flow.
     * Useful in transfer flows where the source account is already authenticated.
     */
    val accessTokens: List<IntegrationAccessToken>? = null,
    /**
     * When `true`, the WebView will load any URL regardless of origin.
     * Intended for testing/debugging only — leave `false` (the default) in production.
     */
    val disableDomainWhiteList: Boolean? = false,
    /**
     * BCP-47 language tag that overrides the UI locale (e.g. `"en"`, `"fr-FR"`).
     * Pass `"system"` to use the device's current locale automatically.
     */
    val language: String? = null,
    /**
     * ISO 4217 currency code shown as the fiat equivalent of crypto amounts
     * inside the Link UI (e.g. `"USD"`, `"EUR"`).
     */
    val displayFiatCurrency: String? = null,
    /**
     * Colour theme applied to the Link UI.
     * Pass [LinkTheme.SYSTEM] to use the device's current theme automatically.
     */
    val theme: LinkTheme? = null,
) {
    companion object {
        /**
         * Builds a [token] from an MFS session token, as returned by
         * `POST /v2/sessions`.
         *
         * A session token is bare and carries no host, so [environment] supplies
         * one. It is required rather than defaulted: a session token belongs to
         * exactly one environment, and quietly assuming production would send a
         * dev token to the wrong Link and fail in a way that is hard to read.
         *
         * Returns a token rather than a whole configuration so it composes with
         * the constructor's named arguments instead of duplicating them:
         *
         * ```
         *  linkLauncher.launch(
         *      LinkConfiguration(
         *          token = LinkConfiguration.linkToken(
         *              sessionToken = "ory_ac_...",
         *              environment = MeshLinkEnvironment.PROD,
         *          ),
         *          language = "en",
         *      )
         *  )
         * ```
         */
        fun linkToken(
            sessionToken: String,
            environment: MeshLinkEnvironment,
        ): String = sessionLinkToken(sessionToken, environment)
    }
}
