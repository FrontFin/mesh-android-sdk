package com.meshconnect.link.utils

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.MalformedURLException
import java.net.URL

class DecodeToURLTest {
    @Before
    fun setUp() {
        mockkStatic(::isAtLeastOreo)
        every { isAtLeastOreo } returns true
    }

    @Test
    fun `verify link decoded from linkToken`() {
        val result = decodeToURL("aHR0cHM6Ly9jb20uYXdlc29tZXVybC9iMmItaWZyYW1l")
        result.isSuccess shouldBeEqualTo true
        result.getOrNull() shouldBeEqualTo URL("https://com.awesomeurl/b2b-iframe")
    }

    @Test
    fun `verify link decoded from url`() {
        val result = decodeToURL("https://com.awesomeurl/b2b-iframe")
        result.isSuccess shouldBeEqualTo true
        result.getOrNull() shouldBeEqualTo URL("https://com.awesomeurl/b2b-iframe")
    }

    @Test
    fun `verify link decode function returns 'Empty linkToken' when empty`() {
        val result = decodeToURL("")
        val exception = result.exceptionOrNull()
        exception?.message shouldBe "Empty source"
    }

    @Test
    fun `verify link decode function returns 'Empty linkToken' when null`() {
        val result = decodeToURL(null)
        val exception = result.exceptionOrNull()
        exception?.message shouldBe "Empty source"
    }

    @Test
    fun `verify decode function returns 'wrong 4-byte ending unit'`() {
        val result = decodeToURL("aHR0cHM6Ly93ZWIuZ2V0Y29kZT0tYQ=")
        val exception = result.exceptionOrNull()
        exception?.message shouldBe "Input byte array has wrong 4-byte ending unit"
    }

    @Test
    fun `verify decode function returns 'Last unit does not have enough valid bits'`() {
        val result = decodeToURL("lorem")
        val exception = result.exceptionOrNull()
        exception?.message shouldBe "Last unit does not have enough valid bits"
    }

    @Test
    fun `verify decode function returns 'MalformedURLException'`() {
        val result = decodeToURL("Dx")
        val exception = result.exceptionOrNull()
        exception shouldBeInstanceOf MalformedURLException::class
    }

    @After
    fun tearDown() {
        unmockkStatic(::isAtLeastOreo)
    }
}
