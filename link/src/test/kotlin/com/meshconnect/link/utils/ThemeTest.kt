package com.meshconnect.link.utils

import android.util.Log
import com.meshconnect.link.R
import com.meshconnect.link.entity.LinkTheme
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.After
import org.junit.Before
import org.junit.Test

class ThemeTest {
    // Base64 of {"th":"dark"}
    private val darkThemeLinkStyle = "eyJ0aCI6ImRhcmsifQ=="

    // Base64 of {"th":"light"}
    private val lightThemeLinkStyle = "eyJ0aCI6ImxpZ2h0In0="

    // Base64 of {"th":""}
    private val emptyThemeLinkStyle = "eyJ0aCI6IiJ9"

    // Base64 of {"other":"value"}
    private val noThemeLinkStyle = "eyJvdGhlciI6InZhbHVlIn0="

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        mockkStatic(::decodeBase64)
        every { Log.e(any(), any(), any()) } returns 0
        every { decodeBase64(any()) } answers { String(java.util.Base64.getDecoder().decode(firstArg<String>())) }
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
        unmockkStatic(::decodeBase64)
    }

    // --- getThemeName ---

    @Test
    fun `getThemeName should return dark for DARK`() {
        getThemeName(LinkTheme.DARK).shouldBeEqualTo(THEME_DARK)
    }

    @Test
    fun `getThemeName should return light for LIGHT`() {
        getThemeName(LinkTheme.LIGHT).shouldBeEqualTo(THEME_LIGHT)
    }

    @Test
    fun `getThemeName should return system for SYSTEM`() {
        getThemeName(LinkTheme.SYSTEM).shouldBeEqualTo(THEME_SYSTEM)
    }

    @Test
    fun `getThemeName should return null for null`() {
        getThemeName(null).shouldBeNull()
    }

    // --- resolveTheme ---

    @Test
    fun `resolveTheme should return dark when theme is dark`() {
        resolveTheme(THEME_DARK) { false } shouldBeEqualTo THEME_DARK
    }

    @Test
    fun `resolveTheme should return light when theme is light`() {
        resolveTheme(THEME_LIGHT) { false } shouldBeEqualTo THEME_LIGHT
    }

    @Test
    fun `resolveTheme should return dark when theme is system and system is dark`() {
        resolveTheme(THEME_SYSTEM) { true } shouldBeEqualTo THEME_DARK
    }

    @Test
    fun `resolveTheme should return light when theme is system and system is light`() {
        resolveTheme(THEME_SYSTEM) { false } shouldBeEqualTo THEME_LIGHT
    }

    @Test
    fun `resolveTheme should return null when theme is null`() {
        resolveTheme(null) { false }.shouldBeNull()
    }

    @Test
    fun `resolveTheme should return null when theme is an unknown value`() {
        resolveTheme("unknown") { false }.shouldBeNull()
    }

    @Test
    fun `resolveTheme should return null when theme is empty`() {
        resolveTheme("") { false }.shouldBeNull()
    }

    // --- isDarkLinkTheme ---

    private val legacyUrl = "https://web.meshconnect.com/b64?link_token=abc"

    private fun envelope(json: String) = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(json.toByteArray())

    private fun latestLinkUrl(theme: String? = null): String {
        val th = theme?.let { ""","th":"$it"""" } ?: ""
        return "https://link.meshpay.com/?token=${envelope("""{"v":1,"c":"code"$th}""")}&clientId=org-1"
    }

    @Test
    fun `isDarkLinkTheme should follow the first th param`() {
        isDarkLinkTheme("$legacyUrl&th=dark&th=light") { false } shouldBeEqualTo true
        isDarkLinkTheme("$legacyUrl&th=light&th=dark") { true } shouldBeEqualTo false
    }

    @Test
    fun `isDarkLinkTheme should prefer th over link_style and the token theme`() {
        isDarkLinkTheme("$legacyUrl&th=light&link_style=$darkThemeLinkStyle") { true } shouldBeEqualTo false
        isDarkLinkTheme(latestLinkUrl("LINK_THEME_DARK") + "&th=light") { true } shouldBeEqualTo false
    }

    @Test
    fun `isDarkLinkTheme should use link_style when th is missing or not light or dark`() {
        isDarkLinkTheme("$legacyUrl&link_style=$darkThemeLinkStyle") { false } shouldBeEqualTo true
        isDarkLinkTheme("$legacyUrl&th=system&link_style=$lightThemeLinkStyle") { true } shouldBeEqualTo false
    }

    @Test
    fun `isDarkLinkTheme should follow the device for a link_style system theme`() {
        every { decodeBase64(any()) } returns """{"th":"system"}"""

        isDarkLinkTheme("$legacyUrl&link_style=x") { true } shouldBeEqualTo true
        isDarkLinkTheme("$legacyUrl&link_style=x") { false } shouldBeEqualTo false
    }

    @Test
    fun `isDarkLinkTheme should default to light for a legacy link without a theme`() {
        isDarkLinkTheme(legacyUrl) { true } shouldBeEqualTo false
        isDarkLinkTheme("$legacyUrl&link_style=$noThemeLinkStyle") { true } shouldBeEqualTo false
        isDarkLinkTheme("$legacyUrl&link_style=$emptyThemeLinkStyle") { true } shouldBeEqualTo false
    }

    @Test
    fun `isDarkLinkTheme should use the latest Link token theme`() {
        isDarkLinkTheme(latestLinkUrl("LINK_THEME_DARK")) { false } shouldBeEqualTo true
        isDarkLinkTheme(latestLinkUrl("LINK_THEME_LIGHT")) { true } shouldBeEqualTo false
    }

    @Test
    fun `isDarkLinkTheme should follow the device for a latest Link token without a theme`() {
        isDarkLinkTheme(latestLinkUrl()) { true } shouldBeEqualTo true
        isDarkLinkTheme(latestLinkUrl()) { false } shouldBeEqualTo false
    }

    @Test
    fun `isDarkLinkTheme should default to light when link_style cannot be decoded`() {
        every { decodeBase64(any()) } throws IllegalArgumentException("bad base64")

        isDarkLinkTheme("$legacyUrl&link_style=%%%") { true } shouldBeEqualTo false
    }

    // --- isLatestLinkUrl ---

    @Test
    fun `isLatestLinkUrl should accept a valid token envelope`() {
        isLatestLinkUrl(latestLinkUrl()) shouldBeEqualTo true
    }

    @Test
    fun `isLatestLinkUrl should reject legacy and malformed tokens`() {
        isLatestLinkUrl(legacyUrl) shouldBeEqualTo false
        isLatestLinkUrl("https://link.meshpay.com/?token=not-json") shouldBeEqualTo false
        isLatestLinkUrl("https://link.meshpay.com/?token=${envelope("""{"v":"1","c":"code"}""")}") shouldBeEqualTo false
        isLatestLinkUrl("https://link.meshpay.com/?token=${envelope("""{"v":1,"c":""}""")}") shouldBeEqualTo false
        isLatestLinkUrl("https://link.meshpay.com/?token=") shouldBeEqualTo false
    }

    // --- themeColor ---

    @Test
    fun `themeColor should use one canvas color for the latest Link`() {
        themeColor(isDarkTheme = true, isLatestLink = true, top = true) shouldBeEqualTo R.color.linkCanvasDark
        themeColor(isDarkTheme = true, isLatestLink = true, top = false) shouldBeEqualTo R.color.linkCanvasDark
        themeColor(isDarkTheme = false, isLatestLink = true, top = true) shouldBeEqualTo R.color.linkCanvasLight
        themeColor(isDarkTheme = false, isLatestLink = true, top = false) shouldBeEqualTo R.color.linkCanvasLight
    }

    @Test
    fun `themeColor should keep the legacy top and bottom colors`() {
        themeColor(isDarkTheme = true, isLatestLink = false, top = true) shouldBeEqualTo R.color.coldGray70
        themeColor(isDarkTheme = true, isLatestLink = false, top = false) shouldBeEqualTo R.color.gray80
        themeColor(isDarkTheme = false, isLatestLink = false, top = true) shouldBeEqualTo R.color.coldGray0
        themeColor(isDarkTheme = false, isLatestLink = false, top = false) shouldBeEqualTo R.color.gray0
    }
}
