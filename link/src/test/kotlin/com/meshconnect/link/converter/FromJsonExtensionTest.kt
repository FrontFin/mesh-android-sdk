package com.meshconnect.link.converter

import com.google.gson.Gson
import com.meshconnect.link.entity.JsError
import com.meshconnect.link.randomString
import org.junit.Test

class FromJsonExtensionTest {
    @Test
    fun `verify 'fromJson' extension`() {
        val msg = randomString
        val actual = Gson().fromJson<JsError>("{'errorMessage':'${msg}'}")
        val expected = JsError(msg)
        assert(actual == expected)

    }
}