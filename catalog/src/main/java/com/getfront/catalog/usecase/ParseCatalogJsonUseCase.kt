package com.getfront.catalog.usecase

import com.getfront.catalog.converter.JsonConverter
import com.getfront.catalog.entity.CatalogResponse
import com.getfront.catalog.entity.FrontAccount
import com.getfront.catalog.entity.JsBrokerageResponse
import com.getfront.catalog.entity.JsBrokerageResponse.Type
import com.getfront.catalog.utils.runCatching
import kotlinx.coroutines.CoroutineDispatcher

internal class ParseCatalogJsonUseCase(
    private val dispatcher: CoroutineDispatcher,
    private val converter: JsonConverter,
) {
    suspend fun launch(json: String) = runCatching(dispatcher) {
        val response = converter.parse<JsBrokerageResponse>(json)

        when (val type = response.type) {
            Type.done -> {
                CatalogResponse.Done
            }
            Type.close -> {
                CatalogResponse.Close
            }
            Type.loaded -> {
                CatalogResponse.Loaded
            }
            Type.brokerageAccountAccessToken -> {
                val payload = requireNotNull(response.payload) { "Empty token payload" }
                val accounts = mapAccounts(payload)
                CatalogResponse.Connected(accounts)
            }
            Type.setTitle -> {
                CatalogResponse.Title(
                    response.title, response.hideBackButton, response.hideTitle
                )
            }
            Type.error -> {
                error(response.errorMessage ?: "Undefined error")
            }
            Type.delayedAuthentication, Type.tradingBrokerConnected -> {
                error("JS message type is not supported yet: $type")
            }
            else -> {
                error("JS message type is unknown: $type")
            }
        }
    }

    private fun mapAccounts(payload: JsBrokerageResponse.Payload): List<FrontAccount> {
        return payload.accountTokens.map {
            FrontAccount(
                accessToken = it.accessToken,
                refreshToken = it.refreshToken,
                accountId = it.account.accountId,
                accountName = it.account.accountName,
                brokerType = payload.brokerType,
                brokerName = payload.brokerName
            )
        }
    }
}
