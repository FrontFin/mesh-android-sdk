package com.meshconnect.link.utils

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test

class DecodeBase64Test {
    @Before
    fun setUp() {
        mockkStatic(::isAtLeastOreo)
        every { isAtLeastOreo } returns true
    }

    @Test
    fun `decodeBase64 should decode base64 token`() {
        val result = decodeBase64("aHR0cHM6Ly9jb20uYXdlc29tZXVybC9iMmItaWZyYW1l")
        result shouldBeEqualTo "https://com.awesomeurl/b2b-iframe"
    }

    @Test(expected = IllegalArgumentException::class)
    fun `decodeToken should throw error on 'Wrong 4-byte ending unit'`() {
        decodeBase64("aHR0cHM6Ly93ZWIuZ2V0Y29kZT0tYQ=")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `decodeToken should throw error on 'Last unit does not have enough valid bits'`() {
        decodeBase64("lorem")
    }

    @After
    fun tearDown() {
        unmockkStatic(::isAtLeastOreo)
    }
}
