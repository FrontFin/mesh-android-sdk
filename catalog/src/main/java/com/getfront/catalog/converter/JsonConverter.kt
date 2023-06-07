package com.getfront.catalog.converter

import com.getfront.catalog.entity.TransferFinishedPayload
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

internal object JsonConverter {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(
            TransferFinishedPayload::class.java,
            TransferFinishedPayloadDeserializer()
        )
        .create()

    internal inline fun <reified T> parse(json: String): T = gson.fromJson(json)
}

internal inline fun <reified T> typeOf(): Type = object : TypeToken<T>() {}.type

internal inline fun <reified T> Gson.fromJson(json: String): T = fromJson(json, typeOf<T>())

internal inline fun <reified T> JsonDeserializationContext.deserialize(json: JsonElement): T =
    deserialize(json, typeOf<T>())
