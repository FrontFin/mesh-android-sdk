package com.meshconnect.link.utils

import org.amshove.kluent.internal.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WhitelistedOriginsTest {
    @Test
    fun `verify the origin is whitelisted`() {
        val website = whitelistedOrigins.firstOrNull { it.startsWith("http") }
        if (website != null) {
            assertTrue(isUrlWhitelisted(website, ""))
        }
        val domain = whitelistedOrigins.firstOrNull { !it.startsWith("http") }
        if (domain != null) {
            assertTrue(isUrlWhitelisted("", "web$domain"))
        }
        assertFalse(isUrlWhitelisted("http://", ""))
        assertFalse(isUrlWhitelisted("https://", ""))
        assertFalse(isUrlWhitelisted("other://", ""))
        assertFalse(isUrlWhitelisted("", ".other.com"))
    }

    @Test
    fun `OKX Wallet's connect host is excluded even though it matches the okx-com suffix`() {
        assertTrue(isUrlWhitelisted("https://www.okx.com", "www.okx.com"))
        assertFalse(isUrlWhitelisted("https://web3.okx.com/download", "web3.okx.com"))
    }

    @Test
    fun `OKX Wallet exclusion matches regardless of host casing`() {
        assertFalse(isUrlWhitelisted("https://WEB3.okx.com/download", "WEB3.okx.com"))
        assertFalse(isUrlWhitelisted("https://Web3.Okx.Com/download", "Web3.Okx.Com"))
    }
}
