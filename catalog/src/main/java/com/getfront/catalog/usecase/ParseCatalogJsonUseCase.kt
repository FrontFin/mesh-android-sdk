package com.getfront.catalog.usecase

import com.getfront.catalog.converter.JsonConverter
import com.getfront.catalog.entity.CatalogEvent
import com.getfront.catalog.entity.FrontAccount
import com.getfront.catalog.entity.JsAccessTokens
import com.getfront.catalog.entity.JsError
import com.getfront.catalog.entity.JsType
import com.getfront.catalog.entity.Payload
import com.getfront.catalog.entity.Type
import com.getfront.catalog.utils.runCatching
import kotlinx.coroutines.CoroutineDispatcher

internal class ParseCatalogJsonUseCase(
    private val dispatcher: CoroutineDispatcher,
    private val converter: JsonConverter,
) {
    suspend fun launch(json: String) = runCatching(dispatcher) {
        val event = converter.parse<JsType>(json)

        when (event.type) {
            Type.done -> CatalogEvent.Done
            Type.close -> CatalogEvent.Close
            Type.showClose -> CatalogEvent.ShowClose
            Type.brokerageAccountAccessToken -> {
                val response = converter.parse<JsAccessTokens>(json)
                val payload = requireNotNull(response.payload) { "Empty token payload" }
                val accounts = mapAccounts(payload)
                CatalogEvent.Connected(accounts)
            }
            Type.error -> {
                val response = converter.parse<JsError>(json)
                error(response.errorMessage ?: "Undefined error")
            }
            else -> CatalogEvent.Undefined
        }
    }

    private fun mapAccounts(payload: Payload): List<FrontAccount> {
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
