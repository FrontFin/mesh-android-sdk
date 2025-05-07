package com.meshconnect.link.utils

import com.meshconnect.link.converter.JsonConverter
import io.mockk.every
import io.mockk.mockkObject
import org.amshove.kluent.internal.assertEquals
import org.junit.Before
import org.junit.Test

class ExtractTrueAuthResultTest {
    @Before
    fun setUp() {
        mockkObject(JsonConverter)
    }

    private fun mockToMap(map: Map<String, *>) {
        every { JsonConverter.toMap(any()) } returns map
    }

    @Test
    fun `extractTrueAuthResult should return result`() {
        mockToMap(mapOf("type" to "trueAuthResult", "result" to "encoded"))
        assertEquals(extractTrueAuthResult(""), "encoded")
    }

    @Test(expected = IllegalStateException::class)
    fun `extractTrueAuthResult should throw exception on wrong type`() {
        mockToMap((mapOf("type" to "lorem")))
        extractTrueAuthResult("")
    }

    @Test(expected = IllegalStateException::class)
    fun `extractTrueAuthResult should throw exception on missing result`() {
        mockToMap((mapOf("type" to "trueAuthResult")))
        extractTrueAuthResult("")
    }

    @Test(expected = IllegalStateException::class)
    fun `extractTrueAuthResult should throw exception on wrong result type`() {
        mockToMap((mapOf("type" to "trueAuthResult", "result" to 100)))
        extractTrueAuthResult("")
    }
}
