package com.meshconnect.link.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkOriginTest {
    @Test
    fun `Mesh Link pages are a Link origin`() {
        assertTrue(isLinkOrigin("https://web.meshconnect.com/b2b-iframe/abc/broker-connect"))
        assertTrue(isLinkOrigin("https://link.meshconnect.com/?token=abc"))
        assertTrue(isLinkOrigin("https://link.meshpay.com/catalog"))
        assertTrue(isLinkOrigin("https://link.dev.meshpay.com"))
        assertTrue(isLinkOrigin("https://LINK.MeshConnect.com/"))
        assertTrue(isLinkOrigin("https://meshconnect.com"))
    }

    @Test
    fun `Link pages with unescaped characters in the query or hash are still a Link origin`() {
        assertTrue(isLinkOrigin("https://link.meshconnect.com/#{\"a\":1}"))
        assertTrue(isLinkOrigin("https://link.meshpay.com/?filter=a|b&x=`y`^"))
    }

    @Test
    fun `third-party pages admitted by the WebView whitelist are not a Link origin`() {
        assertFalse(isLinkOrigin("https://www.coinbase.com/oauth/authorize"))
        assertFalse(isLinkOrigin("https://www.okx.com/account/oauth"))
        assertFalse(isLinkOrigin("https://newassets.hcaptcha.com/captcha/v1"))
        assertFalse(isLinkOrigin("https://accounts.google.com/"))
        assertFalse(isLinkOrigin("https://js.stripe.com/v3"))
    }

    @Test
    fun `lookalike hosts are not a Link origin`() {
        assertFalse(isLinkOrigin("https://link.meshconnect.com.evil.com"))
        assertFalse(isLinkOrigin("https://evilmeshconnect.com"))
        assertFalse(isLinkOrigin("https://meshpay.com.evil.com/catalog"))
        assertFalse(isLinkOrigin("https://link.meshconnect.com@evil.com/"))
    }

    @Test
    fun `non-https pages are not a Link origin`() {
        assertFalse(isLinkOrigin("http://link.meshconnect.com"))
        assertFalse(isLinkOrigin("javascript:alert(1)"))
        assertFalse(isLinkOrigin("data:text/html,<script></script>"))
        assertFalse(isLinkOrigin("about:blank"))
        assertFalse(isLinkOrigin("file:///android_asset/link.meshconnect.com"))
    }

    @Test
    fun `missing or malformed urls are not a Link origin`() {
        assertFalse(isLinkOrigin(null))
        assertFalse(isLinkOrigin(""))
        assertFalse(isLinkOrigin("https://"))
        assertFalse(isLinkOrigin("not a url"))
    }
}
