package com.meshconnect.link.usecase

import com.meshconnect.link.converter.JsonConverter
import com.meshconnect.link.entity.AccessTokenPayload
import com.meshconnect.link.entity.DelayedAuthPayload
import com.meshconnect.link.entity.JsError
import com.meshconnect.link.entity.JsType
import com.meshconnect.link.entity.LinkEvent
import com.meshconnect.link.entity.LinkPayload
import com.meshconnect.link.entity.TransferFinishedPayload
import com.meshconnect.link.entity.Type

internal class DeserializeLinkMessageUseCase(
    private val jsonConverter: JsonConverter
) {
    fun launch(json: String): LinkEvent {
        val type = jsonConverter.fromJson(json, JsType::class.java).type

        return when (type) {
            null -> LinkEvent.Undefined
            Type.close -> LinkEvent.Close
            Type.done -> LinkEvent.Done
            Type.showClose -> LinkEvent.ShowClose
            Type.loaded -> LinkEvent.Loaded
            Type.brokerageAccountAccessToken -> {
                toPayload(json, AccessTokenPayload::class.java)
            }
            Type.transferFinished -> {
                toPayload(json, TransferFinishedPayload::class.java)
            }
            Type.delayedAuthentication -> {
                toPayload(json, DelayedAuthPayload::class.java)
            }
            Type.error -> {
                val errorMessage = toError(json).errorMessage
                error(errorMessage ?: "Error is not defined")
            }
        }
    }

    private inline fun <reified T : LinkPayload> toPayload(json: String, classOfT: Class<T>) =
        LinkEvent.Payload(jsonConverter.fromJson(json, classOfT))

    private fun toError(json: String) = jsonConverter.fromJson(json, JsError::class.java)
}
