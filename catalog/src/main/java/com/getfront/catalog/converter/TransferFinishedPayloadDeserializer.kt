package com.getfront.catalog.converter

import com.getfront.catalog.entity.TransferFinishedErrorPayload
import com.getfront.catalog.entity.TransferFinishedPayload
import com.getfront.catalog.entity.TransferFinishedSuccessPayload
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class TransferFinishedPayloadDeserializer : JsonDeserializer<TransferFinishedPayload> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): TransferFinishedPayload {
        val obj = json.asJsonObject
        return when (val status = obj.get("status").asString) {
            "success" -> context.deserialize<TransferFinishedSuccessPayload>(obj)
            "error" -> context.deserialize<TransferFinishedErrorPayload>(obj)
            else -> error("unknown 'status' $status")
        }
    }
}
