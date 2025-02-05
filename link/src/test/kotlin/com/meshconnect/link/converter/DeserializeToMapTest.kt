package com.meshconnect.link.converter

import org.amshove.kluent.internal.assertFailsWith
import org.amshove.kluent.shouldContainSame
import org.junit.Test

class DeserializeToMapTest {
    private val jsonConverter = JsonConverter

    @Test
    fun `test deserialization to map`() {
        jsonConverter.toMap("{type: loaded}") shouldContainSame mapOf("type" to "loaded")
    }

    @Test
    fun `test empty json throws an exception`() {
        assertFailsWith<NullPointerException> { jsonConverter.toMap("") }
    }
}
