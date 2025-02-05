package com.meshconnect.link.usecase

import com.meshconnect.link.converter.JsonConverter
import com.meshconnect.link.entity.AccessTokenResponse
import com.meshconnect.link.entity.DelayedAuthResponse
import com.meshconnect.link.entity.JsError
import com.meshconnect.link.entity.JsType
import com.meshconnect.link.entity.LinkEvent
import com.meshconnect.link.entity.TransferFinishedResponse
import com.meshconnect.link.entity.Type

internal class DeserializeLinkMessageUseCase(
    private val jsonConverter: JsonConverter,
) {
    fun launch(json: String): LinkEvent? {
        val type = jsonConverter.fromJson(json, JsType::class.java).type

        return when (type) {
            Type.close -> LinkEvent.Close
            Type.done -> LinkEvent.Done
            Type.showClose -> LinkEvent.ShowClose
            Type.loaded -> LinkEvent.Loaded
            Type.brokerageAccountAccessToken -> {
                val response = jsonConverter.fromJson(json, AccessTokenResponse::class.java)
                LinkEvent.Payload(response.payload)
            }
            Type.transferFinished -> {
                val response = jsonConverter.fromJson(json, TransferFinishedResponse::class.java)
                LinkEvent.Payload(response.payload)
            }
            Type.delayedAuthentication -> {
                val response = jsonConverter.fromJson(json, DelayedAuthResponse::class.java)
                LinkEvent.Payload(response.payload)
            }
            Type.error -> {
                val error = jsonConverter.fromJson(json, JsError::class.java)
                error(error.errorMessage ?: "Error is not defined")
            }
            else -> null
        }
    }
}
