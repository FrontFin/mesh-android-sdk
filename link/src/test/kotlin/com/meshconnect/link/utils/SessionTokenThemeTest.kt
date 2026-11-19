package com.meshconnect.link.utils

import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldBeTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Base64

class SessionTokenThemeTest {
    @Before
    fun setUp() {
        mockkStatic(::isAtLeastOreo)
        every { isAtLeastOreo } returns true
        mockkStatic(Uri::class)
    }

    @After
    fun tearDown() {
        unmockkStatic(::isAtLeastOreo)
        unmockkStatic(Uri::class)
    }

    // --- getSessionTokenTheme ---

    @Test
    fun `realistic base64url envelope with LINK_THEME_DARK resolves to dark`() {
        // {"v":1,"c":"AbC123-xYz_789","th":"LINK_THEME_DARK","n":"~~~???"}, base64url without padding.
        val token = envelope("""{"v":1,"c":"AbC123-xYz_789","th":"LINK_THEME_DARK","n":"~~~???"}""")
        (token.contains('-') || token.contains('_')).shouldBeTrue()
        (token.length % 4 != 0).shouldBeTrue()

        getSessionTokenTheme(token) shouldBeEqualTo THEME_DARK
    }

    @Test
    fun `envelope with LINK_THEME_LIGHT resolves to light`() {
        getSessionTokenTheme(envelope("""{"v":1,"c":"code","th":"LINK_THEME_LIGHT"}""")) shouldBeEqualTo THEME_LIGHT
    }

    @Test
    fun `envelope with LINK_THEME_UNSPECIFIED has no theme`() {
        getSessionTokenTheme(envelope("""{"v":1,"c":"code","th":"LINK_THEME_UNSPECIFIED"}""")).shouldBeNull()
    }

    @Test
    fun `envelope without th has no theme`() {
        getSessionTokenTheme(envelope("""{"v":1,"c":"code"}""")).shouldBeNull()
    }

    @Test
    fun `envelope with a non-enum th has no theme`() {
        getSessionTokenTheme(envelope("""{"v":1,"c":"code","th":"dark"}""")).shouldBeNull()
        getSessionTokenTheme(envelope("""{"v":1,"c":"code","th":1}""")).shouldBeNull()
    }

    @Test
    fun `envelope without numeric v is not an envelope`() {
        getSessionTokenTheme(envelope("""{"v":"1","c":"code","th":"LINK_THEME_DARK"}""")).shouldBeNull()
        getSessionTokenTheme(envelope("""{"c":"code","th":"LINK_THEME_DARK"}""")).shouldBeNull()
        getSessionTokenTheme(envelope("""{"v":true,"c":"code","th":"LINK_THEME_DARK"}""")).shouldBeNull()
        getSessionTokenTheme(envelope("""{"v":null,"c":"code","th":"LINK_THEME_DARK"}""")).shouldBeNull()
    }

    @Test
    fun `padded standard base64 envelope is accepted`() {
        val json = """{"v":1,"c":"AbC123-xYz_789","th":"LINK_THEME_LIGHT","n":"~~~???"}"""
        val token = Base64.getEncoder().encodeToString(json.toByteArray())
        (token.contains('+') || token.contains('/')).shouldBeTrue()
        token.endsWith("=").shouldBeTrue()

        getSessionTokenTheme(token) shouldBeEqualTo THEME_LIGHT
    }

    @Test
    fun `padded base64url envelope is accepted`() {
        val json = """{"v":1,"c":"AbC123-xYz_789","th":"LINK_THEME_DARK","n":"~~~???"}"""
        val token = Base64.getUrlEncoder().encodeToString(json.toByteArray())

        getSessionTokenTheme(token) shouldBeEqualTo THEME_DARK
    }

    @Test
    fun `envelope without non-empty string c is not an envelope`() {
        getSessionTokenTheme(envelope("""{"v":1,"c":"","th":"LINK_THEME_DARK"}""")).shouldBeNull()
        getSessionTokenTheme(envelope("""{"v":1,"c":7,"th":"LINK_THEME_DARK"}""")).shouldBeNull()
        getSessionTokenTheme(envelope("""{"v":1,"th":"LINK_THEME_DARK"}""")).shouldBeNull()
    }

    @Test
    fun `json array is not an envelope`() {
        getSessionTokenTheme(envelope("""[{"v":1,"c":"code","th":"LINK_THEME_DARK"}]""")).shouldBeNull()
    }

    @Test
    fun `raw legacy token has no theme`() {
        getSessionTokenTheme("a1b2c3d4-e5f6-7890-abcd-ef1234567890").shouldBeNull()
        getSessionTokenTheme("abcd").shouldBeNull()
    }

    @Test
    fun `malformed token has no theme`() {
        getSessionTokenTheme("%%%not base64%%%").shouldBeNull()
        getSessionTokenTheme("a").shouldBeNull()
        getSessionTokenTheme(envelope("""{"v":1,"c":"code","th":"LINK_THEME_DARK"""")).shouldBeNull()
    }

    @Test
    fun `null or blank token has no theme`() {
        getSessionTokenTheme(null).shouldBeNull()
        getSessionTokenTheme("").shouldBeNull()
        getSessionTokenTheme("   ").shouldBeNull()
    }

    // --- getSessionTokenFromUrl ---

    @Test
    fun `getSessionTokenFromUrl returns the token query param`() {
        val token = envelope("""{"v":1,"c":"code","th":"LINK_THEME_DARK"}""")
        val uri = mockk<Uri>()
        every { uri.getQueryParameter("token") } returns token
        every { Uri.parse(any()) } returns uri

        val sessionToken = getSessionTokenFromUrl("https://web.meshconnect.com/link?token=$token&th=system")

        sessionToken shouldBeEqualTo token
        getSessionTokenTheme(sessionToken) shouldBeEqualTo THEME_DARK
    }

    @Test
    fun `getSessionTokenFromUrl returns null on exception`() {
        every { Uri.parse(any()) } throws RuntimeException("parse error")

        getSessionTokenFromUrl("bad-url").shouldBeNull()
    }

    // --- end to end: URL th + session token → system bars ---

    @Test
    fun `system bars follow the token when the SDK sends th=system`() {
        val token = envelope("""{"v":1,"c":"code","th":"LINK_THEME_DARK"}""")
        resolveSystemBarsTheme(THEME_SYSTEM, { getSessionTokenTheme(token) }) { false } shouldBeEqualTo THEME_DARK
    }

    @Test
    fun `system bars follow the device for a raw token`() {
        resolveSystemBarsTheme(THEME_SYSTEM, { getSessionTokenTheme("raw-code") }) { true } shouldBeEqualTo THEME_DARK
        resolveSystemBarsTheme(null, { getSessionTokenTheme("raw-code") }) { false } shouldBeEqualTo THEME_LIGHT
    }

    @Test
    fun `system bars follow the host override over the token`() {
        val token = envelope("""{"v":1,"c":"code","th":"LINK_THEME_DARK"}""")
        resolveSystemBarsTheme(THEME_LIGHT, { getSessionTokenTheme(token) }) { true } shouldBeEqualTo THEME_LIGHT
    }

    private fun envelope(json: String): String = Base64.getUrlEncoder().withoutPadding().encodeToString(json.toByteArray())
}
