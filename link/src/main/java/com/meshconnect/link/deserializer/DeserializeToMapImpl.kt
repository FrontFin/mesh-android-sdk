package com.meshconnect.link.deserializer

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meshconnect.link.usecase.DeserializeToMap

internal object DeserializeToMapImpl : DeserializeToMap {
    private val gson = Gson()

    override fun invoke(json: String): Map<String, *> {
        return gson.fromJson(json, object : TypeToken<Map<String, *>>() {}.type)
    }
}
