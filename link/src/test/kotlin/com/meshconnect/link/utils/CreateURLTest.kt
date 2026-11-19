import com.meshconnect.link.entity.LinkTheme
import com.meshconnect.link.utils.createLinkURL
import com.meshconnect.link.utils.createURL
import com.meshconnect.link.utils.getThemeName
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.MalformedURLException
import java.net.URL

class CreateURLTest {
    @Test
    fun `createURL should return base URL when queryMap is empty`() {
        val url = createURL("https://example.com", emptyMap())
        assertEquals(URL("https://example.com"), url)
    }

    @Test
    fun `createURL should return base URL with queryMap default param`() {
        val url = createURL("https://example.com")
        assertEquals(URL("https://example.com"), url)
    }

    @Test
    fun `createURL should add a single query param`() {
        val url = createURL("https://example.com", mapOf("key" to "value"))
        assertEquals(URL("https://example.com?key=value"), url)
    }

    @Test
    fun `createURL should append multiple query params`() {
        val url = createURL("https://example.com", mapOf("a" to "1", "b" to "2"))
        assertEquals(URL("https://example.com?a=1&b=2"), url)
    }

    @Test
    fun `createURL should ignore null or empty values`() {
        val url = createURL("https://example.com", mapOf("b" to null, "c" to "", "d" to "2"))
        assertEquals(URL("https://example.com?d=2"), url)
    }

    @Test
    fun `createURL should append query to existing query string`() {
        val url = createURL("https://example.com?existing=1", mapOf("a" to "2"))
        assertEquals(URL("https://example.com?existing=1&a=2"), url)
    }

    @Test(expected = MalformedURLException::class)
    fun `createURL should throw error on malformed url`() {
        createURL("Dx")
    }

    @Test
    fun `createLinkURL should send th=system as-is for LinkTheme SYSTEM`() {
        val url = createLinkURL("https://example.com?token=abc", "en", null, getThemeName(LinkTheme.SYSTEM))
        assertEquals(URL("https://example.com?token=abc&lng=en&th=system&platform=android"), url)
    }

    @Test
    fun `createLinkURL should send th=light and th=dark for LinkTheme LIGHT and DARK`() {
        assertEquals(
            URL("https://example.com?th=light&platform=android"),
            createLinkURL("https://example.com", null, null, getThemeName(LinkTheme.LIGHT)),
        )
        assertEquals(
            URL("https://example.com?th=dark&platform=android"),
            createLinkURL("https://example.com", null, null, getThemeName(LinkTheme.DARK)),
        )
    }

    @Test
    fun `createLinkURL should omit th when theme is null`() {
        val url = createLinkURL("https://example.com", null, "USD", getThemeName(null))
        assertEquals(URL("https://example.com?fiatCur=USD&platform=android"), url)
    }
}
