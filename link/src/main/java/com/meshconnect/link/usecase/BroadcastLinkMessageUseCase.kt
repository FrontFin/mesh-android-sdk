package com.meshconnect.link.usecase

import com.meshconnect.link.EventEmitter
import com.meshconnect.link.utils.runCatching
import kotlinx.coroutines.CoroutineDispatcher

internal class BroadcastLinkMessageUseCase(
    private val dispatcher: CoroutineDispatcher,
    private val deserializeToMap: DeserializeToMap,
    private val filterLinkMessage: FilterLinkMessage,
    private val eventEmitter: EventEmitter
) {
    suspend fun launch(json: String) = runCatching(dispatcher) {
        val map = filterLinkMessage(deserializeToMap(json))
        if (map != null) eventEmitter.emit(map)
    }
}

internal interface DeserializeToMap {
    operator fun invoke(json: String): Map<String, *>
}

internal interface FilterLinkMessage {
    operator fun invoke(map: Map<String, *>): Map<String, *>?
}

internal object FilterLinkMessageImpl : FilterLinkMessage {

    private val typesMap = mapOf(
        "brokerageAccountAccessToken" to "integrationConnected",
        "delayedAuthentication" to "integrationConnected",
        "transferFinished" to "transferCompleted",
        "loaded" to "pageLoaded",
    )

    private val types = listOf(
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
        "transferDeclined"
    )

    override fun invoke(map: Map<String, *>): Map<String, *>? {
        val type = map["type"]
        val typeChange = typesMap[type]
        return when {
            typeChange != null -> map + ("type" to typeChange)
            types.contains(type) -> map
            else -> null
        }
    }
}
