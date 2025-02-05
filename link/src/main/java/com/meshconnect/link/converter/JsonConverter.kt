package com.meshconnect.link.converter

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.meshconnect.link.entity.TransferFinishedPayload

internal object JsonConverter {
    private val gson: Gson by lazy {
        GsonBuilder().registerTypeAdapter(
            TransferFinishedPayload::class.java,
            TransferFinishedPayloadDeserializer(),
        ).create()
    }

    fun <T> fromJson(
        json: String,
        classOfT: Class<T>,
    ): T {
        return gson.fromJson(json, classOfT)
    }

    fun toMap(json: String): Map<String, *> {
        return gson.fromJson(json, object : TypeToken<Map<String, *>>() {}.type)
    }
}
