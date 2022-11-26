package com.getfront.catalog.converter

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

internal object JsonConverter {

    val gson = Gson()

    internal inline fun <reified T> parse(json: String): T = gson.fromJson(json)
}

internal inline fun <reified T> typeOf(): Type = object : TypeToken<T>() {}.type

internal inline fun <reified T> Gson.fromJson(json: String): T = fromJson(json, typeOf<T>())
