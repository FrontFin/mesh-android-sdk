package com.meshconnect.link.utils

import org.junit.Assert.assertFalse
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

    @Test
    fun `a full-URL whitelist entry cannot be spoofed by an attacker-controlled suffix`() {
        assertFalse(isUrlWhitelisted("https://robinhood.com.evil.com/phishing", "robinhood.com.evil.com"))
        assertFalse(isUrlWhitelisted("https://robinhood.comevil.com/phishing", "robinhood.comevil.com"))
    }

    @Test
    fun `a full-URL whitelist entry still matches its real path, query, and exact form`() {
        assertTrue(isUrlWhitelisted("https://robinhood.com", "robinhood.com"))
        assertTrue(isUrlWhitelisted("https://robinhood.com/login", "robinhood.com"))
        assertTrue(isUrlWhitelisted("https://robinhood.com?next=/home", "robinhood.com"))
    }

    @Test
    fun `a legitimately whitelisted domain still matches regardless of host casing`() {
        assertTrue(isUrlWhitelisted("", "WWW.OKX.COM"))
        assertTrue(isUrlWhitelisted("", "Web.MeshConnect.Com"))
    }
}
