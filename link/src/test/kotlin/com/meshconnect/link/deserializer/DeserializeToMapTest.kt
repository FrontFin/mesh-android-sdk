package com.meshconnect.link.deserializer

import org.amshove.kluent.internal.assertFailsWith
import org.amshove.kluent.shouldContainSame
import org.junit.Test

class DeserializeToMapTest {

    private val deserializeToMap = DeserializeToMapImpl

    @Test
    fun `test deserialization to map`() {
        deserializeToMap("{type: loaded}") shouldContainSame mapOf("type" to "loaded")
    }

    @Test
    fun `test empty json throws an exception`() {
        assertFailsWith<NullPointerException> { deserializeToMap("") }
    }
}