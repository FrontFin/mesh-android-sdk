package com.meshconnect.link.utils

import org.amshove.kluent.internal.assertFalse
import org.junit.Test

class WhitelistedOriginsTest {
    @Test
    fun `verify the origin is whitelisted`() {
        val website = whitelistedOrigins.firstOrNull { it.startsWith("http") }
        if (website != null) {
            assert(isUrlWhitelisted(website, ""))
        }
        val domain = whitelistedOrigins.firstOrNull { !it.startsWith("http") }
        if (domain != null) {
            assert(isUrlWhitelisted("", "web$domain"))
        }
        assertFalse(isUrlWhitelisted("http://", ""))
        assertFalse(isUrlWhitelisted("https://", ""))
        assertFalse(isUrlWhitelisted("other://", ""))
        assertFalse(isUrlWhitelisted("", ".other.com"))
    }

    @Test
    fun `MFS Link v3 hosts are whitelisted`() {
        assert(isUrlWhitelisted("", "link.meshpay.com"))
        assert(isUrlWhitelisted("", "link.dev.meshpay.com"))
        assert(isUrlWhitelisted("", "api.meshpay.com"))
    }

    @Test
    fun `meshpay lookalikes are not whitelisted`() {
        // The leading dot in the entry is what stops a lookalike registration
        // such as evilmeshpay.com from satisfying it.
        assertFalse(isUrlWhitelisted("", "evilmeshpay.com"))
        assertFalse(isUrlWhitelisted("", "meshpay.com.evil.com"))
    }
}
