package com.meshconnect.link.utils

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
    private val client = """"v":1,"c":"client""""

    @Before
    fun setUp() {
        mockkStatic(::decodeBase64)
        every { decodeBase64(any()) } answers { String(java.util.Base64.getDecoder().decode(firstArg<String>())) }
    }

    @After
    fun tearDown() {
        unmockkStatic(::decodeBase64)
    }

    private fun appearance(
        url: String,
        isSystemDark: Boolean,
        appTheme: String? = null,
    ) = resolveLinkAppearance(QueryString.parse(url), appTheme, isSystemDark)

    private fun isDark(
        url: String,
        isSystemDark: Boolean,
    ) = appearance(url, isSystemDark).isDarkTheme

    private fun background(
        url: String,
        isSystemDark: Boolean,
    ) = appearance(url, isSystemDark).backgroundColor

    private fun session(extra: String = "") = sessionUrl("{$client}", extra)

    private fun sessionWith(
        field: String,
        extra: String = "",
    ) = sessionUrl("{$client,$field}", extra)

    // --- getThemeName ---

    @Test
    fun `getThemeName should name each app theme`() {
        getThemeName(LinkTheme.DARK) shouldBeEqualTo THEME_DARK
        getThemeName(LinkTheme.LIGHT) shouldBeEqualTo THEME_LIGHT
        getThemeName(LinkTheme.SYSTEM) shouldBeEqualTo THEME_SYSTEM
        getThemeName(null).shouldBeNull()
    }

    // --- theme of a link with a session token ---

    @Test
    fun `session link should prefer th light or dark in the url over the token theme`() {
        isDark(sessionWith(""""th":"LINK_THEME_DARK"""", "&th=light"), true) shouldBeEqualTo false
        isDark(sessionWith(""""th":"LINK_THEME_LIGHT"""", "&th=dark"), false) shouldBeEqualTo true
    }

    @Test
    fun `session link should use the app theme in place of the url th`() {
        val url = sessionWith(""""th":"LINK_THEME_DARK"""", "&th=dark")
        appearance(url, true, appTheme = THEME_LIGHT).isDarkTheme shouldBeEqualTo false
    }

    @Test
    fun `session link should then use the token theme`() {
        isDark(sessionWith(""""th":"LINK_THEME_DARK""""), false) shouldBeEqualTo true
        isDark(sessionWith(""""th":"LINK_THEME_LIGHT""""), true) shouldBeEqualTo false
    }

    @Test
    fun `session link should let the token theme win over th system`() {
        isDark(sessionWith(""""th":"LINK_THEME_LIGHT"""", "&th=system"), true) shouldBeEqualTo false
        isDark(sessionWith(""""th":"LINK_THEME_DARK"""", "&th=system"), false) shouldBeEqualTo true
    }

    @Test
    fun `session link should then follow the device whether th is system, missing or garbage`() {
        for (extra in listOf("", "&th=system", "&th=blue", "&th=")) {
            isDark(session(extra), true) shouldBeEqualTo true
            isDark(session(extra), false) shouldBeEqualTo false
        }
    }

    @Test
    fun `session link should follow the device for a token theme that is unspecified or unknown`() {
        for (th in listOf(""""LINK_THEME_UNSPECIFIED"""", """"constructor"""", "7", "null")) {
            isDark(sessionWith(""""th":$th"""), true) shouldBeEqualTo true
            isDark(sessionWith(""""th":$th"""), false) shouldBeEqualTo false
        }
    }

    @Test
    fun `session link should not read link_style`() {
        val url = session(linkStyleParam("""{"th":"dark"}"""))
        isDark(url, false) shouldBeEqualTo false
    }

    @Test
    fun `a token that is not a session token is a legacy link, so its theme is ignored`() {
        val notSession =
            listOf(
                """{"c":"client","th":"LINK_THEME_DARK"}""",
                """{"v":"1","c":"client","th":"LINK_THEME_DARK"}""",
                """{"v":1,"c":"","th":"LINK_THEME_DARK"}""",
                """{"v":1,"th":"LINK_THEME_DARK"}""",
            )
        for (envelope in notSession) {
            isDark(sessionUrl(envelope), false) shouldBeEqualTo true
        }
        isDark("https://link.meshpay.com/?token=not-json", false) shouldBeEqualTo true
    }

    // --- theme of a legacy link ---

    @Test
    fun `legacy link should prefer th light or dark in the url over link_style`() {
        isDark(legacyUrl("""{"th":"dark"}""", "&th=light"), true) shouldBeEqualTo false
        isDark(legacyUrl("""{"th":"light"}""", "&th=dark"), false) shouldBeEqualTo true
    }

    @Test
    fun `legacy link should read th system as the device even when link_style has a theme`() {
        isDark(legacyUrl("""{"th":"dark"}""", "&th=system"), false) shouldBeEqualTo false
        isDark(legacyUrl("""{"th":"light"}""", "&th=system"), true) shouldBeEqualTo true
    }

    @Test
    fun `legacy link should then use link_style th, with system read from the device`() {
        isDark(legacyUrl("""{"th":"light"}"""), true) shouldBeEqualTo false
        isDark(legacyUrl("""{"th":"dark"}"""), false) shouldBeEqualTo true
        isDark(legacyUrl("""{"th":"light"}""", "&th=blue"), true) shouldBeEqualTo false
        isDark(legacyUrl("""{"th":"system"}"""), false) shouldBeEqualTo false
        isDark(legacyUrl("""{"th":"system"}"""), true) shouldBeEqualTo true
    }

    @Test
    fun `legacy link should read a link_style with a stripped padding or a plus read as a space`() {
        // Base64 of {"th":"light","x":"?>"} has a '+'.
        val withPlus = "eyJ0aCI6ImxpZ2h0IiwieCI6Ij8+In0="

        fun url(value: String) = "https://link.meshconnect.com/?link_style=$value"
        isDark(url(withPlus.trimEnd('=')), true) shouldBeEqualTo false
        isDark(url(withPlus.replace('+', ' ')), true) shouldBeEqualTo false
    }

    @Test
    fun `legacy link should then default to dark, even on a light device`() {
        isDark(legacyUrl(), false) shouldBeEqualTo true
        isDark("https://link.meshconnect.com/", false) shouldBeEqualTo true
        for (value in listOf("", "%%%", "bm90IGpzb24=", "e30=", "eyJ0aCI6IiJ9")) {
            isDark("https://link.meshconnect.com/?link_style=$value", false) shouldBeEqualTo true
        }
    }

    // --- theme to write ---

    @Test
    fun `themeToWrite should be the device theme on a legacy link the device decides`() {
        appearance(legacyUrl("""{"th":"system"}"""), true).themeToWrite shouldBeEqualTo THEME_DARK
        appearance(legacyUrl(), true, appTheme = THEME_SYSTEM).themeToWrite shouldBeEqualTo THEME_DARK
        appearance(legacyUrl(extra = "&th=system"), false).themeToWrite shouldBeEqualTo THEME_LIGHT
    }

    @Test
    fun `themeToWrite should be null when the device does not decide or the link has a session token`() {
        appearance(legacyUrl(), true).themeToWrite.shouldBeNull()
        appearance(legacyUrl("""{"th":"light"}"""), true).themeToWrite.shouldBeNull()
        appearance(session(), true).themeToWrite.shouldBeNull()
        appearance(session(), true, appTheme = THEME_SYSTEM).themeToWrite.shouldBeNull()
    }

    // --- background color ---

    @Test
    fun `background should be the page bgd on a dark theme and bgl on a light theme, from the token`() {
        val url = { th: String -> sessionWith(""""bgl":"#112233","bgd":"#445566"""", "&th=$th") }
        background(url("dark"), false) shouldBeEqualTo 0xFF445566.toInt()
        background(url("light"), true) shouldBeEqualTo 0xFF112233.toInt()
    }

    @Test
    fun `background should come from link_style on a legacy link`() {
        val url = { th: String -> legacyUrl("""{"bgl":"#112233","bgd":"#445566"}""", "&th=$th") }
        background(url("dark"), false) shouldBeEqualTo 0xFF445566.toInt()
        background(url("light"), true) shouldBeEqualTo 0xFF112233.toInt()
    }

    @Test
    fun `background should expand a short hex and drop the alpha`() {
        fun color(bgd: String) = background(sessionWith(""""bgd":"$bgd"""", "&th=dark"), false)
        color("#abc") shouldBeEqualTo 0xFFAABBCC.toInt()
        color("#abcd") shouldBeEqualTo 0xFFAABBCC.toInt()
        color("#11223344") shouldBeEqualTo 0xFF112233.toInt()
        color("#ABCDEF") shouldBeEqualTo 0xFFABCDEF.toInt()
    }

    @Test
    fun `background should fall back when the color is missing or invalid`() {
        val invalid = listOf("\"red\"", "\"rgb(1,2,3)\"", "\"#12\"", "\"#12345\"", "\"#ggg\"", "\"\"", "123", "null")
        for (bad in invalid) {
            background(sessionWith(""""bgd":$bad""", "&th=dark"), false) shouldBeEqualTo 0xFF1C1C1C.toInt()
        }
    }

    @Test
    fun `background should fall back for each theme on its own`() {
        val onlyLight = sessionWith(""""bgl":"#123456"""")
        background("$onlyLight&th=dark", false) shouldBeEqualTo 0xFF1C1C1C.toInt()
        background("$onlyLight&th=light", true) shouldBeEqualTo 0xFF123456.toInt()
    }

    @Test
    fun `background should use the pair of the Link version`() {
        background(session("&th=light"), true) shouldBeEqualTo 0xFFF9F9F9.toInt()
        background(legacyUrl(), false) shouldBeEqualTo 0xFF0E0D0D.toInt()
        background(legacyUrl("""{"th":"light"}"""), true) shouldBeEqualTo 0xFFFBFBFB.toInt()
    }

    // --- isDarkColor ---

    @Test
    fun `isDarkColor should tell dark colors from light ones`() {
        isDarkColor(0xFF1C1C1C.toInt()) shouldBeEqualTo true
        isDarkColor(0xFF0E0D0D.toInt()) shouldBeEqualTo true
        isDarkColor(0xFF000000.toInt()) shouldBeEqualTo true
        isDarkColor(0xFFF9F9F9.toInt()) shouldBeEqualTo false
        isDarkColor(0xFFFFFFFF.toInt()) shouldBeEqualTo false
        // a client's light page color on a dark theme
        isDarkColor(0xFFEEEEEE.toInt()) shouldBeEqualTo false
        // a mid green: black icons have the better contrast (8.1 against 2.6)
        isDarkColor(0xFF00BB00.toInt()) shouldBeEqualTo false
        isDarkColor(0xFF1A2440.toInt()) shouldBeEqualTo true
    }
}
