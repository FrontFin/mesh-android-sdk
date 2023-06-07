package com.getfront.catalog.usecase

import com.getfront.catalog.converter.JsonConverter
import com.getfront.catalog.entity.AccessTokenResponse
import com.getfront.catalog.entity.JsError
import com.getfront.catalog.entity.JsType
import com.getfront.catalog.entity.LinkEvent
import com.getfront.catalog.entity.TransferFinishedResponse
import com.getfront.catalog.entity.Type
import com.getfront.catalog.utils.printStackTrace
import com.getfront.catalog.utils.runCatching
import kotlinx.coroutines.CoroutineDispatcher

internal class GetLinkEventUseCase(
    private val dispatcher: CoroutineDispatcher,
    private val converter: JsonConverter,
) {
    suspend fun launch(json: String) = runCatching(dispatcher) {
        val event = converter.parse<JsType>(json)

        when (event.type) {
            Type.done -> LinkEvent.Done
            Type.close -> LinkEvent.Close
            Type.showClose -> LinkEvent.ShowClose
            Type.brokerageAccountAccessToken -> onAccessToken(json)
            Type.transferFinished -> onTransferFinished(json)
            Type.error -> onError(json)
            else -> LinkEvent.Undefined
        }
    }

    private fun onAccessToken(json: String) = try {
        val payload = converter.parse<AccessTokenResponse>(json).payload
        LinkEvent.Payload(payload)
    } catch (expected: Exception) {
        printStackTrace(expected)
        error("Faced an error while parsing access token payload: ${expected.message}")
    }

    private fun onTransferFinished(json: String) = try {
        val payload = converter.parse<TransferFinishedResponse>(json).payload
        LinkEvent.Payload(payload)
    } catch (expected: Exception) {
        printStackTrace(expected)
        error("Faced an error while parsing transfer finished payload: ${expected.message}")
    }

    private fun onError(json: String): Nothing {
        val response = converter.parse<JsError>(json)
        error(response.errorMessage ?: "Undefined error")
    }
}
