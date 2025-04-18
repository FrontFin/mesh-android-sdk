package com.meshconnect.link.utils

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test

class DecodeTokenTest {
    @Before
    fun setUp() {
        mockkStatic(::decodeBase64)
        every { decodeBase64(any()) } returns "base64"
    }

    @Test
    fun `decodeToken should return token when it starts with http`() {
        val token = "http://example.com/token"
        val result = decodeToken(token)
        result shouldBeEqualTo token
    }

    @Test
    fun `decodeToken should decode base64 token`() {
        val result = decodeToken("aHR0cHM6Ly9jb20uYXdlc29tZXVybC9iMmItaWZyYW1l")
        result shouldBeEqualTo "base64"
    }

    @Test(expected = IllegalStateException::class)
    fun `decodeToken should throw error when token is null`() {
        decodeToken(null)
    }

    @Test(expected = IllegalStateException::class)
    fun `decodeToken should throw error when token is empty`() {
        decodeToken("")
    }

    @After
    fun tearDown() {
        unmockkStatic(::decodeBase64)
    }
}
