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
        every { Log.w(any(), any<String>(), any()) } returns 0
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

    // --- resolveLinkTheme ---

    private val dark = { true }
    private val light = { false }

    @Test
    fun `resolveLinkTheme should prefer the app theme over the url and link_style`() {
        resolveLinkTheme(THEME_LIGHT, THEME_DARK, darkThemeLinkStyle, dark) shouldBeEqualTo THEME_LIGHT
        resolveLinkTheme(THEME_DARK, THEME_LIGHT, lightThemeLinkStyle, light) shouldBeEqualTo THEME_DARK
    }

    @Test
    fun `resolveLinkTheme should resolve a system app theme from the device`() {
        resolveLinkTheme(THEME_SYSTEM, THEME_LIGHT, null, dark) shouldBeEqualTo THEME_DARK
        resolveLinkTheme(THEME_SYSTEM, THEME_DARK, null, light) shouldBeEqualTo THEME_LIGHT
    }

    @Test
    fun `resolveLinkTheme should use the url th when the app sets none`() {
        resolveLinkTheme(null, THEME_DARK, lightThemeLinkStyle, light) shouldBeEqualTo THEME_DARK
        resolveLinkTheme(null, THEME_LIGHT, darkThemeLinkStyle, dark) shouldBeEqualTo THEME_LIGHT
        resolveLinkTheme(null, THEME_SYSTEM, null, light) shouldBeEqualTo THEME_LIGHT
    }

    @Test
    fun `resolveLinkTheme should use link_style when there is no usable th`() {
        resolveLinkTheme(null, null, darkThemeLinkStyle, light) shouldBeEqualTo THEME_DARK
        resolveLinkTheme(null, "unknown", lightThemeLinkStyle, dark) shouldBeEqualTo THEME_LIGHT
    }

    @Test
    fun `resolveLinkTheme should resolve a system link_style from the device`() {
        val systemLinkStyle = "eyJ0aCI6InN5c3RlbSJ9" // {"th":"system"}
        resolveLinkTheme(null, null, systemLinkStyle, dark) shouldBeEqualTo THEME_DARK
        resolveLinkTheme(null, null, systemLinkStyle, light) shouldBeEqualTo THEME_LIGHT
    }

    @Test
    fun `resolveLinkTheme should restore a link_style whose plus turned into a space`() {
        // Base64 of {"th":"dark","x":"~~~"} has a '+' that URL decoding turns into a space.
        val linkStyle = java.util.Base64.getEncoder().encodeToString("""{"th":"dark","x":"~~~"}""".toByteArray())
        resolveLinkTheme(null, null, linkStyle.replace('+', ' ').trimEnd('='), light) shouldBeEqualTo THEME_DARK
    }

    @Test
    fun `resolveLinkTheme should default to dark when nothing is set`() {
        resolveLinkTheme(null, null, null, light) shouldBeEqualTo THEME_DARK
        resolveLinkTheme(null, null, noThemeLinkStyle, light) shouldBeEqualTo THEME_DARK
        resolveLinkTheme(null, null, emptyThemeLinkStyle, light) shouldBeEqualTo THEME_DARK
    }

    @Test
    fun `resolveLinkTheme should default to dark when link_style cannot be read`() {
        resolveLinkTheme(null, null, "%%%", light) shouldBeEqualTo THEME_DARK
    }

    // --- systemBarColor ---

    @Test
    fun `systemBarColor should use one color per theme`() {
        systemBarColor(isDarkTheme = true) shouldBeEqualTo R.color.linkBarDark
        systemBarColor(isDarkTheme = false) shouldBeEqualTo R.color.linkBarLight
    }
}
