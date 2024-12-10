package com.meshconnect.link.converter

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.meshconnect.link.entity.TransferFinishedErrorPayload
import com.meshconnect.link.entity.TransferFinishedPayload
import com.meshconnect.link.entity.TransferFinishedSuccessPayload
import java.lang.reflect.Type

internal class TransferFinishedPayloadDeserializer : JsonDeserializer<TransferFinishedPayload> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): TransferFinishedPayload {
        val obj = json.asJsonObject.apply {
            addPropertyIfAbsent("fromAddress", "")
            addPropertyIfAbsent("toAddress", "")
            addPropertyIfAbsent("networkName", "")
            addPropertyIfAbsent("txHash", "")
            addPropertyIfAbsent("amountInFiat", 0)
            addPropertyIfAbsent("totalAmountInFiat", 0)
        }
        return when (val status = obj.get("status").asString) {
            "success" -> context.deserialize<TransferFinishedSuccessPayload>(obj)
            "error" -> context.deserialize<TransferFinishedErrorPayload>(obj)
            else -> error("unknown status '$status'")
        }
    }

    private fun JsonObject.addPropertyIfAbsent(name: String, value: Any) {
        when {
            value is String && !has(name) -> addProperty(name, value)
            value is Number && !has(name) -> addProperty(name, value)
        }
    }
}
