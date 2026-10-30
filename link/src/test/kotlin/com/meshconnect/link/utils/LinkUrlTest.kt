package com.meshconnect.link.utils

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test

class LinkUrlTest {
    private val dark = { true }
    private val light = { false }

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
        isSystemThemeDark: () -> Boolean = light,
    ) = buildLinkURL(url, LinkUrlOptions(theme, language, fiatCurrency), isSystemThemeDark)

    @Test
    fun `buildLinkURL should replace the token th with the app theme`() {
        val result = build("https://link.meshpay.com/?token=abc&th=dark&clientId=org-1", theme = THEME_LIGHT)

        result.url.toString() shouldBeEqualTo "https://link.meshpay.com/?token=abc&clientId=org-1&th=light&platform=android"
        result.isDarkTheme shouldBeEqualTo false
    }

    @Test
    fun `buildLinkURL should send a system app theme as the device theme`() {
        build("https://a.com/?token=abc", theme = THEME_SYSTEM, isSystemThemeDark = dark).url.toString() shouldBeEqualTo
            "https://a.com/?token=abc&th=dark&platform=android"
    }

    @Test
    fun `buildLinkURL should keep the token th when the app sets none`() {
        val result = build("https://a.com/?th=light&token=abc")

        result.url.toString() shouldBeEqualTo "https://a.com/?th=light&token=abc&platform=android"
        result.isDarkTheme shouldBeEqualTo false
    }

    @Test
    fun `buildLinkURL should report the link_style theme without writing th`() {
        // Base64 of {"th":"system"}.
        val result = build("https://a.com/?link_style=eyJ0aCI6InN5c3RlbSJ9", isSystemThemeDark = dark)

        result.url.toString() shouldBeEqualTo "https://a.com/?link_style=eyJ0aCI6InN5c3RlbSJ9&platform=android"
        result.isDarkTheme shouldBeEqualTo true
    }

    @Test
    fun `buildLinkURL should report dark when nothing sets a theme, without writing th`() {
        val result = build("https://a.com/?token=abc")

        result.url.toString() shouldBeEqualTo "https://a.com/?token=abc&platform=android"
        result.isDarkTheme shouldBeEqualTo true
    }

    @Test
    fun `buildLinkURL should replace every param it sets, not just th`() {
        val result =
            build(
                "https://a.com/?lng=fr&token=abc&fiatCur=GBP&platform=web&lng=es",
                theme = THEME_DARK,
                language = "de-DE",
                fiatCurrency = "EUR",
            )

        result.url.toString() shouldBeEqualTo "https://a.com/?token=abc&th=dark&lng=de-DE&fiatCur=EUR&platform=android"
    }

    @Test
    fun `buildLinkURL should keep the token language and currency when the app sets none`() {
        build("https://a.com/?lng=fr&fiatCur=GBP").url.toString() shouldBeEqualTo
            "https://a.com/?lng=fr&fiatCur=GBP&platform=android"
    }

    @Test
    fun `buildLinkURL should keep the other params, empty parts and the fragment as they are`() {
        val flow = "{%22flow%22%3A%22login%22}"
        build("https://a.com/?token=a+b%2F&&flow=$flow&th=dark#x").url.toString() shouldBeEqualTo
            "https://a.com/?token=a+b%2F&&flow=$flow&th=dark&platform=android#x"
    }

    @Test
    fun `buildLinkURL should add a query when the url has none`() {
        build("https://a.com/path#x").url.toString() shouldBeEqualTo "https://a.com/path?platform=android#x"
    }

    @Test
    fun `buildLinkURL should match an encoded key when replacing th`() {
        build("https://a.com/?t%68=light&th=dark", theme = THEME_DARK).url.toString() shouldBeEqualTo
            "https://a.com/?th=dark&platform=android"
    }

    @Test
    fun `buildLinkURL should read the first th for the theme`() {
        build("https://a.com/?t%68=light&th=dark").isDarkTheme shouldBeEqualTo false
    }

    @Test
    fun `buildLinkURL should encode the values it adds`() {
        build("https://a.com/", fiatCurrency = "E R&").url.toString() shouldBeEqualTo
            "https://a.com/?fiatCur=E+R%26&platform=android"
    }

    @Test
    fun `buildLinkURL should ignore a blank language and currency`() {
        build("https://a.com/", language = " ", fiatCurrency = "").url.toString() shouldBeEqualTo
            "https://a.com/?platform=android"
    }

    @Test
    fun `buildLinkURL should keep a malformed escape as is`() {
        val result = build("https://a.com/?th=%zz")

        result.url.toString() shouldBeEqualTo "https://a.com/?th=%zz&platform=android"
        result.isDarkTheme shouldBeEqualTo true
    }
}
