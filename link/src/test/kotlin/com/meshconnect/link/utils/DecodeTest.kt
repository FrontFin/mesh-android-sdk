package com.meshconnect.link.utils

import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Test
import java.net.MalformedURLException

class DecodeTest {
    @Test
    fun `verify link decoded from linkToken`() {
        val result = decodeCatching("aHR0cHM6Ly9jb20uYXdlc29tZXVybC9iMmItaWZyYW1l")
        result.isSuccess shouldBeEqualTo true
        result.getOrNull() shouldBeEqualTo "https://com.awesomeurl/b2b-iframe"
    }

    @Test
    fun `verify link decoded from url`() {
        val result = decodeCatching("https://com.awesomeurl/b2b-iframe")
        result.isSuccess shouldBeEqualTo true
        result.getOrNull() shouldBeEqualTo "https://com.awesomeurl/b2b-iframe"
    }

    @Test
    fun `verify link decode function returns 'Empty linkToken'`() {
        val result = decodeCatching("")
        val exception = result.exceptionOrNull()
        exception?.message shouldBe "Empty 'catalogLink' or 'linkToken'"
    }

    @Test
    fun `verify decode function returns 'wrong 4-byte ending unit'`() {
        val result = decodeCatching("aHR0cHM6Ly93ZWIuZ2V0Y29kZT0tYQ=")
        val exception = result.exceptionOrNull()
        exception?.message shouldBe "Input byte array has wrong 4-byte ending unit"
    }

    @Test
    fun `verify decode function returns 'Last unit does not have enough valid bits'`() {
        val result = decodeCatching("lorem")
        val exception = result.exceptionOrNull()
        exception?.message shouldBe "Last unit does not have enough valid bits"
    }

    @Test
    fun `verify decode function returns 'MalformedURLException'`() {
        val result = decodeCatching("Dx")
        val exception = result.exceptionOrNull()
        exception shouldBeInstanceOf MalformedURLException::class
    }
}