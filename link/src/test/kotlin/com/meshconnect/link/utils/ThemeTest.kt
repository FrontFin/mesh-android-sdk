package com.meshconnect.link.utils

import android.net.Uri
import android.util.Log
import com.meshconnect.link.entity.LinkTheme
import com.meshconnect.link.randomString
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.After
import org.junit.Before
import org.junit.Test

class ThemeTest {
    // Mocked url
    private val url = randomString

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
        mockkStatic(Uri::class)
        mockkStatic(Log::class)
        mockkStatic(::decodeBase64)
        every { Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Uri::class)
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

    // --- getThemeFromUrl ---

    @Test
    fun `getThemeFromUrl should return th param when present`() {
        val mockUri = mockUri(th = "dark")
        every { Uri.parse(any()) } returns mockUri

        getThemeFromUrl(url) shouldBeEqualTo "dark"
    }

    @Test
    fun `getThemeFromUrl should return light from th param`() {
        val mockUri = mockUri(th = "light")
        every { Uri.parse(any()) } returns mockUri

        getThemeFromUrl(url) shouldBeEqualTo "light"
    }

    @Test
    fun `getThemeFromUrl should return th from link_style JSON when th param is absent`() {
        val mockUri = mockUri(th = null, linkStyle = darkThemeLinkStyle)
        every { Uri.parse(any()) } returns mockUri
        every { decodeBase64(darkThemeLinkStyle) } returns """{"th":"dark"}"""

        getThemeFromUrl(url) shouldBeEqualTo "dark"
    }

    @Test
    fun `getThemeFromUrl should return light from link_style JSON`() {
        val mockUri = mockUri(th = null, linkStyle = lightThemeLinkStyle)
        every { Uri.parse(any()) } returns mockUri
        every { decodeBase64(lightThemeLinkStyle) } returns """{"th":"light"}"""

        getThemeFromUrl(url) shouldBeEqualTo "light"
    }

    @Test
    fun `getThemeFromUrl should prefer th param over link_style`() {
        val mockUri = mockUri(th = "light", linkStyle = darkThemeLinkStyle)
        every { Uri.parse(any()) } returns mockUri

        getThemeFromUrl(url) shouldBeEqualTo "light"
    }

    @Test
    fun `getThemeFromUrl should fall back to link_style when th param is blank`() {
        val mockUri = mockUri(th = "", linkStyle = darkThemeLinkStyle)
        every { Uri.parse(any()) } returns mockUri
        every { decodeBase64(darkThemeLinkStyle) } returns """{"th":"dark"}"""

        getThemeFromUrl(url) shouldBeEqualTo "dark"
    }

    @Test
    fun `getThemeFromUrl should return null when th is absent and link_style has no th key`() {
        val mockUri = mockUri(th = null, linkStyle = noThemeLinkStyle)
        every { Uri.parse(any()) } returns mockUri
        every { decodeBase64(noThemeLinkStyle) } returns """{"other":"value"}"""

        getThemeFromUrl(url).shouldBeNull()
    }

    @Test
    fun `getThemeFromUrl should return null when th is absent and link_style th value is empty`() {
        val mockUri = mockUri(th = null, linkStyle = emptyThemeLinkStyle)
        every { Uri.parse(any()) } returns mockUri
        every { decodeBase64(emptyThemeLinkStyle) } returns """{"th":""}"""

        getThemeFromUrl(url).shouldBeNull()
    }

    @Test
    fun `getThemeFromUrl should return null when both th and link_style are absent`() {
        val mockUri = mockUri(th = null, linkStyle = null)
        every { Uri.parse(any()) } returns mockUri

        getThemeFromUrl(url).shouldBeNull()
    }

    @Test
    fun `getThemeFromUrl should return null and log error on exception`() {
        every { Uri.parse(any()) } throws RuntimeException("parse error")

        getThemeFromUrl("bad-url").shouldBeNull()
    }

    // --- helpers ---

    private fun mockUri(
        th: String?,
        linkStyle: String? = null,
    ): Uri {
        return mockk<Uri>().also { uri ->
            every { uri.getQueryParameter("th") } returns th
            every { uri.getQueryParameter("link_style") } returns linkStyle
        }
    }
}
