package com.meshconnect.link.converter

import com.meshconnect.link.entity.JsError
import com.meshconnect.link.readFile
import org.junit.Test

class JsErrorTest {

    private val gson = JsonConverter.get()

    @Test
    fun testJsError() {
        val json = readFile("js-error.json")
        val actual = gson.fromJson<JsError>(json)
        val expected = JsError("unable to connect the broker")
        assert(actual == expected)
    }
}