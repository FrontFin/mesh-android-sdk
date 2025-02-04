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
}
