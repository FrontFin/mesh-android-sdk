package com.meshconnect.link.entity

import com.meshconnect.link.randomString
import io.mockk.mockk
import org.amshove.kluent.internal.assertEquals
import org.junit.Test

class LinkConfigurationTest {
    @Test
    fun `test LinkConfiguration`() {
        val token = randomString
        val accessTokens = mockk<List<IntegrationAccessToken>>()
        val transferDestinationTokens = mockk<List<IntegrationAccessToken>>()

        val it = LinkConfiguration(
            token,
            accessTokens,
            transferDestinationTokens
        )

        assertEquals(token, it.token)
        assertEquals(accessTokens, it.accessTokens)
        assertEquals(transferDestinationTokens, it.transferDestinationTokens)
    }

    @Test
    fun `test LinkConfiguration default constructor`() {
        val token = randomString

        val it = LinkConfiguration(
            token
        )

        assertEquals(token, it.token)
        assertEquals(null, it.accessTokens)
        assertEquals(null, it.transferDestinationTokens)
    }
}