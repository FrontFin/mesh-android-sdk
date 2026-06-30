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
    private const val INTEGRATION_CONNECTED = "integrationConnected"

    // Event types that must never carry a payload to the public LinkEvents stream.
    private val strippedPayloadTypes = setOf(INTEGRATION_CONNECTED)

    private val typesMap =
        mapOf(
            "brokerageAccountAccessToken" to INTEGRATION_CONNECTED,
            "delayedAuthentication" to INTEGRATION_CONNECTED,
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
            "defiWalletError",
            "paypalComplianceDeclined",
            "verifyDonePage",
            "verifyWalletRejected",
            "integrationMfaRequired",
            "integrationMfaEntered",
            "integrationOAuthStarted",
            "integrationAccountSelectionRequired",
            "transferAmountEntered",
            "transferMfaRequired",
            "transferMfaEntered",
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
            "linkTransferQRGenerated",
            "methodSelected",
            "homePageLoaded",
        )

    fun filter(map: Map<String, *>): Map<String, *>? {
        val type = map["type"]
        val typeChange = typesMap[type]
        val outputType = typeChange ?: type
        return when {
            outputType in strippedPayloadTypes -> mapOf("type" to outputType)
            typeChange != null -> map + ("type" to typeChange)
            types.contains(type) -> map
            else -> null
        }
    }
}
