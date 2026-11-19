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

    // --- resolveSystemBarsTheme ---

    @Test
    fun `resolveSystemBarsTheme should use dark url theme over token and device`() {
        resolveSystemBarsTheme(THEME_DARK, { THEME_LIGHT }) { false } shouldBeEqualTo THEME_DARK
    }

    @Test
    fun `resolveSystemBarsTheme should use light url theme over token and device`() {
        resolveSystemBarsTheme(THEME_LIGHT, { THEME_DARK }) { true } shouldBeEqualTo THEME_LIGHT
    }

    @Test
    fun `resolveSystemBarsTheme should use token theme when url theme is system`() {
        resolveSystemBarsTheme(THEME_SYSTEM, { THEME_DARK }) { false } shouldBeEqualTo THEME_DARK
        resolveSystemBarsTheme(THEME_SYSTEM, { THEME_LIGHT }) { true } shouldBeEqualTo THEME_LIGHT
    }

    @Test
    fun `resolveSystemBarsTheme should use token theme when url theme is null`() {
        resolveSystemBarsTheme(null, { THEME_DARK }) { false } shouldBeEqualTo THEME_DARK
        resolveSystemBarsTheme(null, { THEME_LIGHT }) { true } shouldBeEqualTo THEME_LIGHT
    }

    @Test
    fun `resolveSystemBarsTheme should use device when url theme is system and token has no theme`() {
        resolveSystemBarsTheme(THEME_SYSTEM, { null }) { true } shouldBeEqualTo THEME_DARK
        resolveSystemBarsTheme(THEME_SYSTEM, { null }) { false } shouldBeEqualTo THEME_LIGHT
    }

    @Test
    fun `resolveSystemBarsTheme should use device when url theme is null and token has no theme`() {
        resolveSystemBarsTheme(null, { null }) { true } shouldBeEqualTo THEME_DARK
        resolveSystemBarsTheme(null, { null }) { false } shouldBeEqualTo THEME_LIGHT
    }

    @Test
    fun `resolveSystemBarsTheme should treat an unknown url theme as no preference`() {
        resolveSystemBarsTheme("unknown", { THEME_DARK }) { false } shouldBeEqualTo THEME_DARK
        resolveSystemBarsTheme("", { null }) { true } shouldBeEqualTo THEME_DARK
    }

    @Test
    fun `resolveSystemBarsTheme should not read the token when url theme is an override`() {
        resolveSystemBarsTheme(THEME_LIGHT, { error("token must not be read") }) { true } shouldBeEqualTo THEME_LIGHT
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
    fun `getThemeFromUrl should return the last th param like Link does`() {
        val mockUri = mockUri(th = listOf("dark", "system"))
        every { Uri.parse(any()) } returns mockUri

        getThemeFromUrl(url) shouldBeEqualTo THEME_SYSTEM
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
    ): Uri = mockUri(listOfNotNull(th), linkStyle)

    private fun mockUri(
        th: List<String>,
        linkStyle: String? = null,
    ): Uri {
        return mockk<Uri>().also { uri ->
            every { uri.getQueryParameters("th") } returns th
            every { uri.getQueryParameter("link_style") } returns linkStyle
        }
    }
}
