package com.meshconnect.link.usecase

import com.meshconnect.link.EventEmitter
import com.meshconnect.link.converter.JsonConverter

internal class BroadcastLinkMessageUseCase(
    private val jsonConverter: JsonConverter,
    private val filterLinkMessage: FilterLinkMessage,
    private val eventEmitter: EventEmitter,
) {
    suspend fun launch(json: String) {
        val map = filterLinkMessage.filter(jsonConverter.toMap(json))
        if (map != null) eventEmitter.emit(map)
    }
}

internal object FilterLinkMessage {
    private val typesMap =
        mapOf(
            "brokerageAccountAccessToken" to "integrationConnected",
            "delayedAuthentication" to "integrationConnected",
            "transferFinished" to "transferCompleted",
            "loaded" to "pageLoaded",
        )

    private val types =
        listOf(
            "integrationConnectionError",
            "integrationSelected",
            "credentialsEntered",
            "transferStarted",
            "transferExecuted",
            "transferInitiated",
            "transferPreviewed",
            "transferPreviewError",
            "transferExecutionError",
            "transferNoEligibleAssets",
            "walletMessageSigned",
            "verifyDonePage",
            "verifyWalletRejected",
            "integrationMfaRequired",
            "integrationMfaEntered",
            "integrationOAuthStarted",
            "integrationAccountSelectionRequired",
            "transferAmountEntered",
            "transferMfaRequired",
            "transferKycRequired",
            "connectionDeclined",
            "transferConfigureError",
            "transferAssetSelected",
            "transferNetworkSelected",
            "connectionUnavailable",
            "transferDeclined",
            "legalTermsViewed",
            "seeWhatHappenedClicked",
            "executeFundingStep",
            "fundingOptionsUpdated",
            "fundingOptionsViewed",
            "gasIncreaseWarning",
        )

    fun filter(map: Map<String, *>): Map<String, *>? {
        val type = map["type"]
        val typeChange = typesMap[type]
        return when {
            typeChange != null -> map + ("type" to typeChange)
            types.contains(type) -> map
            else -> null
        }
    }
}
