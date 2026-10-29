package com.meshconnect.link.utils

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.Test

class QueryParamsTest {
    private val token = "eyJ2IjoxLCJjIjoiYWJjIn0"

    @Test
    fun `getFirstQueryParam should return the first value`() {
        getFirstQueryParam("https://a.com/?th=dark&th=light", "th") shouldBeEqualTo "dark"
    }

    @Test
    fun `getFirstQueryParam should decode the value and match a decoded key`() {
        getFirstQueryParam("https://a.com/?t%68=a%20b", "th") shouldBeEqualTo "a b"
    }

    @Test
    fun `getFirstQueryParam should ignore the fragment`() {
        getFirstQueryParam("https://a.com/?a=1#th=dark", "th").shouldBeNull()
    }

    @Test
    fun `getFirstQueryParam should return empty for a key without a value`() {
        getFirstQueryParam("https://a.com/?th", "th") shouldBeEqualTo ""
    }

    @Test
    fun `getFirstQueryParam should return null without a query`() {
        getFirstQueryParam("https://a.com/", "th").shouldBeNull()
    }

    @Test
    fun `getFirstQueryParam should keep a malformed escape as is`() {
        getFirstQueryParam("https://a.com/?th=%zz", "th") shouldBeEqualTo "%zz"
    }

    @Test
    fun `removeQueryParam should remove every value and keep the rest byte for byte`() {
        val flow = "{%22flow%22%3A%22login%22}"
        val url = "https://link.meshpay.com/?token=$token&th=dark&flow=$flow&th=light&clientId=org-1"

        removeQueryParam(url, "th") shouldBeEqualTo
            "https://link.meshpay.com/?token=$token&flow=$flow&clientId=org-1"
    }

    @Test
    fun `removeQueryParam should match an encoded key`() {
        removeQueryParam("https://a.com/?t%68=dark&a=1", "th") shouldBeEqualTo "https://a.com/?a=1"
    }

    @Test
    fun `removeQueryParam should drop the question mark when nothing is left`() {
        removeQueryParam("https://a.com/?th=dark", "th") shouldBeEqualTo "https://a.com/"
    }

    @Test
    fun `removeQueryParam should keep the fragment`() {
        removeQueryParam("https://a.com/?th=dark&a=1#th=x", "th") shouldBeEqualTo "https://a.com/?a=1#th=x"
    }

    @Test
    fun `removeQueryParam should leave a URL without a query unchanged`() {
        removeQueryParam("https://a.com/#th=x", "th") shouldBeEqualTo "https://a.com/#th=x"
    }
}
