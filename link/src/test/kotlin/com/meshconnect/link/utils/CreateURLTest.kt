import com.meshconnect.link.utils.createLinkURL
import com.meshconnect.link.utils.createURL
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

    @Test
    fun `createURL should add params before the fragment`() {
        val url = createURL("https://example.com?a=1#section", mapOf("th" to "dark"))
        assertEquals("https://example.com?a=1&th=dark#section", url.toString())
    }

    @Test
    fun `createLinkURL should replace the token th with the app theme`() {
        val url = createLinkURL("https://link.meshpay.com/?token=abc&th=dark&clientId=org-1", "de-DE", "EUR", "light")
        assertEquals(
            "https://link.meshpay.com/?token=abc&clientId=org-1&lng=de-DE&fiatCur=EUR&th=light&platform=android",
            url.toString(),
        )
    }

    @Test
    fun `createLinkURL should keep the token th when the app passes no theme`() {
        val url = createLinkURL("https://link.meshpay.com/?token=abc&th=dark", null, null, null)
        assertEquals("https://link.meshpay.com/?token=abc&th=dark&platform=android", url.toString())
    }

    @Test(expected = MalformedURLException::class)
    fun `createURL should throw error on malformed url`() {
        createURL("Dx")
    }
}
