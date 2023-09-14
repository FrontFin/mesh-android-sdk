package com.getfront.catalog.usecase

import androidx.annotation.VisibleForTesting
import com.getfront.catalog.converter.fromJson
import com.getfront.catalog.entity.AccessTokenResponse
import com.getfront.catalog.entity.JsError
import com.getfront.catalog.entity.JsType
import com.getfront.catalog.entity.LinkEvent
import com.getfront.catalog.entity.TransferFinishedResponse
import com.getfront.catalog.entity.Type
import com.getfront.catalog.utils.printStackTrace
import com.getfront.catalog.utils.runCatching
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher

internal class GetLinkEventUseCase(
    private val dispatcher: CoroutineDispatcher,
    private val converter: Gson,
) {
    suspend fun launch(json: String) = runCatching(dispatcher) {
        val event = converter.fromJson<JsType>(json)

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

    @VisibleForTesting
    fun onAccessToken(json: String) = try {
        val payload = converter.fromJson<AccessTokenResponse>(json).payload
        LinkEvent.Payload(payload)
    } catch (expected: Exception) {
        printStackTrace(expected)
        error("Faced an error while parsing access token payload: ${expected.message}")
    }

    @VisibleForTesting
    fun onTransferFinished(json: String) = try {
        val payload = converter.fromJson<TransferFinishedResponse>(json).payload
        LinkEvent.Payload(payload)
    } catch (expected: Exception) {
        printStackTrace(expected)
        error("Faced an error while parsing transfer finished payload: ${expected.message}")
    }

    @VisibleForTesting
    fun onError(json: String): Nothing {
        val response = converter.fromJson<JsError>(json)
        error(response.errorMessage ?: "Undefined error")
    }
}
