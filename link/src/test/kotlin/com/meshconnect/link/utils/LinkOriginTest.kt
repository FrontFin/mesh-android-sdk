package com.meshconnect.link.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.URL

class LinkOriginTest {
    @Test
    fun `origin keeps scheme host and port only`() {
        assertEquals("https://link.meshconnect.com", URL("https://LINK.MeshConnect.com/a?b=c#d").origin())
        assertEquals("https://link.meshpay.com", URL("https://link.meshpay.com/?token=abc").origin())
        assertEquals("http://10.0.2.2:3000", URL("http://10.0.2.2:3000/?token=abc").origin())
    }

    @Test
    fun `origin keeps Link URLs with unescaped characters in the query or hash`() {
        assertEquals("https://link.meshconnect.com", URL("https://link.meshconnect.com/#{\"a\":1}").origin())
        assertEquals("https://link.meshpay.com", URL("https://link.meshpay.com/?filter=a|b&x=`y`^").origin())
    }
}
