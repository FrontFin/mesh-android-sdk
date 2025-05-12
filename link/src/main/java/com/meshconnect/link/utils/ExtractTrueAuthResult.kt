package com.meshconnect.link.utils

import com.meshconnect.link.converter.JsonConverter

@Throws(IllegalStateException::class)
internal fun extractTrueAuthResult(json: String): String {
    val map = JsonConverter.toMap(json)

    val type = map["type"]
    if (type != "trueAuthResult") error("wrong type: $type")

    val result = map["result"]
    if (result !is String) error("missing result, actual: $result")

    return result
}
