package com.meshconnect.link.utils

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldContain
import org.junit.After
import org.junit.Before
import org.junit.Test

class LinkUrlTest {
    private val sessionLight = """{"v":1,"c":"client","th":"LINK_THEME_LIGHT"}"""
    private val sessionBase = sessionUrl(sessionLight).substringBefore("&flow")
    private val flow = "flow=%7B%22flow%22%3A%22login%22%7D"
    private val sessionLink = "$sessionBase&$flow"
    private val legacyLink = "https://link.meshconnect.com/?link_style=eyJ0aCI6ImxpZ2h0In0%3D&x=%7B%7D"

    @Before
    fun setUp() {
        mockkStatic(::decodeBase64)
        every { decodeBase64(any()) } answers { String(java.util.Base64.getDecoder().decode(firstArg<String>())) }
    }

    @After
    fun tearDown() {
        unmockkStatic(::decodeBase64)
    }

    private fun build(
        url: String,
        theme: String? = null,
        language: String? = null,
        fiatCurrency: String? = null,
        isSystemThemeDark: Boolean = false,
    ) = buildLinkURL(url, LinkUrlOptions(theme, language, fiatCurrency), isSystemThemeDark)

    private fun urlOf(
        url: String,
        theme: String? = null,
        isSystemThemeDark: Boolean = false,
    ) = build(url, theme = theme, isSystemThemeDark = isSystemThemeDark).url.toString()

    @Test
    fun `buildLinkURL should replace th, lng, fiatCur and platform already in the token and keep the rest`() {
        val result =
            build(
                "$sessionLink&th=dark&lng=fr&fiatCur=EUR&platform=web#f",
                theme = THEME_LIGHT,
                language = "de",
                fiatCurrency = "USD",
            )

        result.url.toString() shouldBeEqualTo "$sessionLink&th=light&lng=de&fiatCur=USD&platform=android#f"
        result.isDarkTheme shouldBeEqualTo false
    }

    @Test
    fun `buildLinkURL should leave one th when the token has th twice`() {
        val url = urlOf("$sessionLink&th=dark&th=dark", theme = THEME_LIGHT)

        url.split("th=").size - 1 shouldBeEqualTo 1
        url shouldContain "&th=light"
    }

    @Test
    fun `buildLinkURL should keep the token th, which decides the native theme, when the app sets none`() {
        val result = build("$sessionLink&th=dark")

        result.url.toString() shouldBeEqualTo "$sessionLink&th=dark&platform=android"
        result.isDarkTheme shouldBeEqualTo true
        result.backgroundColor shouldBeEqualTo 0xFF1C1C1C.toInt()

        val legacy = build(legacyLink, isSystemThemeDark = true)
        legacy.url.toString() shouldBeEqualTo "$legacyLink&platform=android"
        legacy.isDarkTheme shouldBeEqualTo false
        legacy.backgroundColor shouldBeEqualTo 0xFFFBFBFB.toInt()
    }

    @Test
    fun `buildLinkURL should send system as system and follow the token, then the device`() {
        for (systemIsDark in listOf(true, false)) {
            val result = build(sessionLink, theme = THEME_SYSTEM, isSystemThemeDark = systemIsDark)
            result.url.toString() shouldContain "&th=system&"
            // the token says light, so the device does not matter
            result.isDarkTheme shouldBeEqualTo false

            val noTokenTheme = sessionUrl("""{"v":1,"c":"client"}""")
            build(noTokenTheme, theme = THEME_SYSTEM, isSystemThemeDark = systemIsDark).isDarkTheme shouldBeEqualTo systemIsDark
            build(legacyLink, theme = THEME_SYSTEM, isSystemThemeDark = systemIsDark).isDarkTheme shouldBeEqualTo systemIsDark
        }
    }

    @Test
    fun `buildLinkURL should write light or dark on a legacy link the device decides`() {
        val systemStyle = legacyUrl("""{"th":"system"}""")
        val cases = listOf(legacyLink to THEME_SYSTEM, systemStyle to null, "$legacyLink&th=system" to null)
        for ((link, appTheme) in cases) {
            build(link, theme = appTheme, isSystemThemeDark = true).isDarkTheme shouldBeEqualTo true
            urlOf(link, appTheme, isSystemThemeDark = true) shouldContain "&th=dark&"
            urlOf(link, appTheme, isSystemThemeDark = false) shouldContain "&th=light&"
        }
    }

    @Test
    fun `buildLinkURL should not write th on a legacy link the device does not decide`() {
        urlOf(legacyLink, isSystemThemeDark = true) shouldBeEqualTo "$legacyLink&platform=android"
        urlOf("https://link.meshconnect.com/") shouldBeEqualTo "https://link.meshconnect.com/?platform=android"
    }

    @Test
    fun `buildLinkURL should keep system as system on a link with a session token`() {
        urlOf(sessionLink, THEME_SYSTEM, isSystemThemeDark = true) shouldContain "&th=system&"
        urlOf(sessionLink, isSystemThemeDark = true) shouldBeEqualTo "$sessionLink&platform=android"
    }

    @Test
    fun `buildLinkURL should replace the th dark the backend adds on silent migration with system`() {
        urlOf("$sessionLink&th=dark", THEME_SYSTEM, isSystemThemeDark = true) shouldBeEqualTo "$sessionLink&th=system&platform=android"
    }

    @Test
    fun `buildLinkURL should not send a theme it does not know`() {
        urlOf(sessionLink, "blue") shouldBeEqualTo "$sessionLink&platform=android"
    }

    @Test
    fun `buildLinkURL should return the page color from the token`() {
        val url = sessionUrl("""{"v":1,"c":"client","bgl":"#112233","bgd":"#445566"}""")
        build(url, theme = THEME_DARK).backgroundColor shouldBeEqualTo 0xFF445566.toInt()
        build(url, theme = THEME_LIGHT, isSystemThemeDark = true).backgroundColor shouldBeEqualTo 0xFF112233.toInt()
    }

    @Test
    fun `buildLinkURL should keep the other params, empty parts and the fragment as they are`() {
        val flowParam = "{%22flow%22%3A%22login%22}"
        urlOf("https://a.com/?token=a+b%2F&&flow=$flowParam&th=dark#x") shouldBeEqualTo
            "https://a.com/?token=a+b%2F&&flow=$flowParam&th=dark&platform=android#x"
    }

    @Test
    fun `buildLinkURL should add a query when the url has none`() {
        urlOf("https://a.com/path#x") shouldBeEqualTo "https://a.com/path?platform=android#x"
    }

    @Test
    fun `buildLinkURL should match an encoded key and read the first th`() {
        urlOf("https://a.com/?t%68=light&th=dark", THEME_DARK) shouldBeEqualTo "https://a.com/?th=dark&platform=android"
        build("https://a.com/?t%68=light&th=dark").isDarkTheme shouldBeEqualTo false
    }

    @Test
    fun `buildLinkURL should send the system language as given and trim the values`() {
        build(sessionLink, language = " pt-BR ", fiatCurrency = " EUR ").url.toString() shouldContain "&lng=pt-BR&fiatCur=EUR&"
    }

    @Test
    fun `buildLinkURL should encode the values it adds`() {
        build("https://a.com/", fiatCurrency = "E R&").url.toString() shouldBeEqualTo "https://a.com/?fiatCur=E+R%26&platform=android"
    }

    @Test
    fun `buildLinkURL should not send a blank language or currency`() {
        build(sessionLink, language = " ", fiatCurrency = " ").url.toString() shouldBeEqualTo "$sessionLink&platform=android"
        resolveLanguage(" ").shouldBeNull()
    }

    @Test
    fun `buildLinkURL should keep a malformed escape as is`() {
        val result = build("https://a.com/?th=%zz")

        result.url.toString() shouldBeEqualTo "https://a.com/?th=%zz&platform=android"
        result.isDarkTheme shouldBeEqualTo true
    }
}
