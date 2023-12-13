package com.meshconnect.link.usecase

import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.meshconnect.link.converter.fromJson
import com.meshconnect.link.entity.AccessTokenResponse
import com.meshconnect.link.entity.JsError
import com.meshconnect.link.entity.JsType
import com.meshconnect.link.entity.LinkEvent
import com.meshconnect.link.entity.TransferFinishedResponse
import com.meshconnect.link.entity.Type
import com.meshconnect.link.utils.runCatching
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
            Type.loaded -> LinkEvent.Loaded
            else -> LinkEvent.Undefined
        }
    }

    @VisibleForTesting
    fun onAccessToken(json: String) = try {
        val payload = converter.fromJson<AccessTokenResponse>(json).payload
        LinkEvent.Payload(payload)
    } catch (expected: Exception) {
        error("Faced an error while parsing access token payload: ${expected.message}")
    }

    @VisibleForTesting
    fun onTransferFinished(json: String) = try {
        val payload = converter.fromJson<TransferFinishedResponse>(json).payload
        LinkEvent.Payload(payload)
    } catch (expected: Exception) {
        error("Faced an error while parsing transfer finished payload: ${expected.message}")
    }

    @VisibleForTesting
    fun onError(json: String): Nothing {
        val response = converter.fromJson<JsError>(json)
        error(response.errorMessage ?: "Undefined error")
    }
}
