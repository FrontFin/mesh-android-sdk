package com.getfront.catalog.utils

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class DecodeLinkTest {
    @Test
    fun `verify link decoded correctly`() {
        val linkToken = "aHR0cHM6Ly93ZWIuZ2V0ZnJvbnQuY29tL2IyYi1pZnJhbWU="
        val link = decodeLink(linkToken)
        link shouldBeEqualTo "https://web.getfront.com/b2b-iframe"
    }
}