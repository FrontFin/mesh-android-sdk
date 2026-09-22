package com.meshconnect.link

import com.meshconnect.link.converter.JsonConverter
import com.meshconnect.link.entity.AccessTokenPayload
import com.meshconnect.link.entity.LinkConfiguration
import com.meshconnect.link.entity.LinkEvent
import com.meshconnect.link.entity.MeshLinkEnvironment
import com.meshconnect.link.entity.TransferFinishedErrorPayload
import com.meshconnect.link.entity.TransferFinishedSuccessPayload
import com.meshconnect.link.usecase.DeserializeLinkMessageUseCase
import com.meshconnect.link.utils.createURL
import com.meshconnect.link.utils.decodeToken
import com.meshconnect.link.utils.isAtLeastOreo
import com.meshconnect.link.utils.isUrlWhitelisted
import com.meshconnect.link.utils.sessionLinkUrl
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.amshove.kluent.internal.assertFailsWith
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldNotBeNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.URI
import java.net.URLDecoder
import java.util.Base64

/**
 * Cross-SDK MFS parity suite.
 *
 * The same numbered cases (P1.x, P2.x, ...) exist in every mobile SDK, so
 * "parity" is something that can be pointed at rather than asserted. Keep the
 * case ids and the fixtures below identical across repos; when one SDK's
 * behaviour has to differ, keep the case and document why in its body rather
 * than deleting it.
 *
 * The contract being pinned:
 *   P1  a link token is base64 of a URL, so the token alone decides which Link
 *       loads. v1, v2 and MFS all arrive through this one path.
 *   P2  the origin allowlist must accept both meshconnect and meshpay, and must
 *       still reject lookalikes.
 *   P3  Link v3 emits only four legacy events, one of which (`close`) carries no
 *       payload at all.
 *   P4  the v1/v2 payload shapes must keep working throughout the migration.
 */
class MfsParityTest {
    // JsonConverter is an object, so this exercises the real Gson wiring
    // (including the TransferFinishedPayload deserializer) rather than a mock.
    private val deserialize = DeserializeLinkMessageUseCase(JsonConverter)

    // decodeBase64 branches on isAtLeastOreo to pick java.util.Base64 over
    // android.util.Base64, which is unavailable in JVM unit tests. Same mock the
    // existing DecodeBase64Test uses, so the real decode path is exercised.
    @Before
    fun setUp() {
        mockkStatic(::isAtLeastOreo)
        every { isAtLeastOreo } returns true
    }

    @After
    fun tearDown() {
        unmockkStatic(::isAtLeastOreo)
    }

    private fun tokenFor(url: String): String = Base64.getEncoder().encodeToString(url.toByteArray())

    private fun hostOf(url: String): String = URI(url).host

    // -----------------------------------------------------------------------
    // P1  token resolution: the token decides which Link loads
    // -----------------------------------------------------------------------

    @Test
    fun `P1_1 a v1 token resolves to the v1 host`() {
        hostOf(decodeToken(tokenFor(V1_URL))) shouldBeEqualTo "web.meshconnect.com"
    }

    @Test
    fun `P1_2 a v2 token resolves to the v2 host`() {
        hostOf(decodeToken(tokenFor(V2_URL))) shouldBeEqualTo "link.meshconnect.com"
    }

    @Test
    fun `P1_3 an MFS token resolves to the MFS host, session token intact`() {
        val url = decodeToken(tokenFor(MFS_URL))
        hostOf(url) shouldBeEqualTo "link.meshpay.com"
        url.contains("token=ory_ac_abc123").shouldBeTrue()
    }

    @Test
    fun `P1_4 SDK params are appended without dropping token params`() {
        val url =
            createURL(
                decodeToken(tokenFor(V2_URL)),
                mapOf("platform" to "android", "lng" to "es", "fiatCur" to "EUR"),
            ).toString()

        url.contains("platform=android").shouldBeTrue()
        url.contains("lng=es").shouldBeTrue()
        url.contains("fiatCur=EUR").shouldBeTrue()
        // The token's own params survive.
        url.contains("clientId=abc").shouldBeTrue()
        url.contains("auth_code=xyz").shouldBeTrue()
    }

    @Test
    fun `P1_5 a malformed token does not yield a loadable URL`() {
        assertFailsWith<IllegalStateException> { decodeToken(null) }
        assertFailsWith<IllegalStateException> { decodeToken("") }
    }

    // -----------------------------------------------------------------------
    // P2  origin allowlist accepts both platforms
    // -----------------------------------------------------------------------

    @Test
    fun `P2_1 meshconnect hosts are allowed`() {
        isUrlWhitelisted("", "link.meshconnect.com").shouldBeTrue()
        isUrlWhitelisted("", "web.meshconnect.com").shouldBeTrue()
    }

    @Test
    fun `P2_2 meshpay MFS hosts are allowed`() {
        isUrlWhitelisted("", "link.meshpay.com").shouldBeTrue()
        isUrlWhitelisted("", "link.dev.meshpay.com").shouldBeTrue()
        isUrlWhitelisted("", "api.meshpay.com").shouldBeTrue()
    }

    @Test
    fun `P2_3 lookalike hosts are rejected`() {
        // Android's entries carry a leading dot (".meshpay.com") and are matched
        // with host.endsWith, so the dot boundary holds here. Flutter's wildcard
        // matcher has no such boundary; see its P2.5.
        isUrlWhitelisted("", "evilmeshpay.com") shouldBeEqualTo false
        isUrlWhitelisted("", "evilmeshconnect.com") shouldBeEqualTo false
        isUrlWhitelisted("", "meshpay.com.evil.com") shouldBeEqualTo false
    }

    @Test
    fun `P2_4 unrelated hosts are rejected`() {
        isUrlWhitelisted("https://example.com", "example.com") shouldBeEqualTo false
    }

    // Android closed this gap: matchesUrlPrefix requires the character after an
    // explicit (non-dotted) entry to start a path, query, fragment or port, so
    // a host that merely begins with one no longer passes. The case is kept
    // because iOS still matches these entries with a bare hasPrefix and so
    // still accepts the same URL; see its P2_5.
    @Test
    fun `P2_5 explicit entries cannot be spoofed by an attacker-controlled suffix`() {
        isUrlWhitelisted("https://robinhood.com.evil.com", "robinhood.com.evil.com")
            .shouldBeFalse()
    }

    // -----------------------------------------------------------------------
    // P3  Link v3 emits only four legacy events
    // -----------------------------------------------------------------------

    @Test
    fun `P3_1 loaded is recognised`() {
        deserialize.launch("""{"type":"loaded"}""") shouldBeEqualTo LinkEvent.Loaded
    }

    @Test
    fun `P3_2 close arrives with NO payload and must still close`() {
        // v1/v2 always attach an event summary; v3 sends {"type":"close"} alone.
        // Android keys on the type only, so this already held before MFS.
        deserialize.launch("""{"type":"close"}""") shouldBeEqualTo LinkEvent.Close
    }

    @Test
    fun `P3_3 brokerageAccountAccessToken carries the connectionId`() {
        val event = deserialize.launch(V3_BROKER_TOKENS)
        event.shouldBeInstanceOf<LinkEvent.Payload>()

        val payload = (event as LinkEvent.Payload).payload
        payload.shouldBeInstanceOf<AccessTokenPayload>()

        val token = (payload as AccessTokenPayload).accountTokens.first()
        // The contract change: this is a connection handle, not a token.
        token.accessToken shouldBeEqualTo "conn_123"
        token.refreshToken.shouldBeNull()
    }

    @Test
    fun `P3_4 transferFinished parses the v3 success-only shape`() {
        val event = deserialize.launch(V3_TRANSFER_FINISHED)
        event.shouldBeInstanceOf<LinkEvent.Payload>()

        val payload = (event as LinkEvent.Payload).payload
        payload.shouldBeInstanceOf<TransferFinishedSuccessPayload>()
        (payload as TransferFinishedSuccessPayload).txId shouldBeEqualTo "tx_1"
    }

    // -----------------------------------------------------------------------
    // P4  v1/v2 payloads keep working during the migration
    // -----------------------------------------------------------------------

    @Test
    fun `P4_1 close with a payload is still a close`() {
        deserialize.launch("""{"type":"close","payload":{"page":"catalog"}}""")
            .shouldNotBeNull() shouldBeEqualTo LinkEvent.Close
    }

    @Test
    fun `P4_2 legacy brokerageAccountAccessToken still parses`() {
        val event = deserialize.launch(LEGACY_BROKER_TOKENS)
        val payload = (event as LinkEvent.Payload).payload as AccessTokenPayload
        val token = payload.accountTokens.first()

        token.accessToken shouldBeEqualTo "real-access-token"
        token.refreshToken shouldBeEqualTo "real-refresh-token"
    }

    @Test
    fun `P4_3 the v2 transferFinished error branch still parses`() {
        // v3 has no error variant, so this asserts the v2 path is untouched.
        val event = deserialize.launch(LEGACY_TRANSFER_FINISHED_ERROR)
        val payload = (event as LinkEvent.Payload).payload
        payload.shouldBeInstanceOf<TransferFinishedErrorPayload>()
    }

    // -----------------------------------------------------------------------
    // P5  the MFS-native session entry point
    // -----------------------------------------------------------------------

    @Test
    fun `P5_1 a session token becomes a link token for the chosen environment`() {
        mapOf(
            MeshLinkEnvironment.PROD to "https://link.meshpay.com/?token=ory_ac_abc",
            MeshLinkEnvironment.SBX to "https://link.sbx.meshpay.com/?token=ory_ac_abc",
            MeshLinkEnvironment.DEV to "https://link.dev.meshpay.com/?token=ory_ac_abc",
        ).forEach { (environment, expected) ->
            sessionLinkUrl("ory_ac_abc", environment) shouldBeEqualTo expected
        }
    }

    @Test
    fun `P5_2 reserved characters are encoded rather than truncating the URL`() {
        // Interpolating raw would cut the URL at `&` and lose the rest.
        val url = sessionLinkUrl("abc&x=1#frag", MeshLinkEnvironment.PROD)
        url shouldBeEqualTo "https://link.meshpay.com/?token=abc%26x%3D1%23frag"
        // And it survives a round trip intact.
        URLDecoder.decode(url.substringAfter("token="), "UTF-8") shouldBeEqualTo "abc&x=1#frag"
    }

    @Test
    fun `P5_3 a space percent-encodes rather than becoming the form-encoding plus`() {
        sessionLinkUrl("a b", MeshLinkEnvironment.PROD) shouldBeEqualTo
            "https://link.meshpay.com/?token=a%20b"
    }

    @Test
    fun `P5_4 the session configuration resolves to the MFS host`() {
        val configuration =
            LinkConfiguration(
                token =
                    LinkConfiguration.linkToken(
                        sessionToken = "ory_ac_abc",
                        environment = MeshLinkEnvironment.PROD,
                    ),
            )
        decodeToken(configuration.token) shouldBeEqualTo
            "https://link.meshpay.com/?token=ory_ac_abc"
    }

    @Test
    fun `P5_5 an empty session token is rejected rather than opening Link with none`() {
        assertFailsWith<IllegalArgumentException> {
            LinkConfiguration.linkToken(
                sessionToken = "",
                environment = MeshLinkEnvironment.PROD,
            )
        }
    }

    // -----------------------------------------------------------------------
    // Shared fixtures. Keep byte-identical across SDKs.
    // -----------------------------------------------------------------------

    private companion object {
        const val V1_URL = "https://web.meshconnect.com/broker-connect/catalog"
        const val V2_URL = "https://link.meshconnect.com/?clientId=abc&auth_code=xyz"
        const val MFS_URL = "https://link.meshpay.com/?token=ory_ac_abc123"

        /**
         * Link v3 brokerageAccountAccessToken. The connection-id architecture
         * keeps real tokens server-side, so accessToken/accountId/tokenId all
         * carry the connectionId and there is no refreshToken.
         */
        val V3_BROKER_TOKENS =
            """
            {"type":"brokerageAccountAccessToken","payload":{
              "accountTokens":[{
                "account":{"accountId":"conn_123","accountName":"Coinbase"},
                "accessToken":"conn_123","tokenId":"conn_123"}],
              "brokerBrandInfo":{"logoLightUrl":"https://cdn/l.png"},
              "brokerType":"coinbase","brokerName":"Coinbase"}}
            """.trimIndent()

        /** Link v1/v2 shape: a real access token and the older brand fields. */
        val LEGACY_BROKER_TOKENS =
            """
            {"type":"brokerageAccountAccessToken","payload":{
              "accountTokens":[{
                "account":{"accountId":"acc_1","accountName":"Coinbase"},
                "accessToken":"real-access-token",
                "refreshToken":"real-refresh-token","tokenId":"tok_1"}],
              "brokerBrandInfo":{"brokerLogo":"https://cdn/logo.png"},
              "expiresInSeconds":3600,
              "brokerType":"coinbase","brokerName":"Coinbase"}}
            """.trimIndent()

        /** Link v3 transferFinished: success-only and v1-shaped. */
        val V3_TRANSFER_FINISHED =
            """
            {"type":"transferFinished","payload":{
              "status":"success","txId":"tx_1","transferId":"tx_1",
              "fromAddress":"0xfrom","toAddress":"0xto","symbol":"USDC",
              "amount":10.5,"networkId":"base","networkName":"base"}}
            """.trimIndent()

        /** Link v2 transferFinished, error branch. v3 has no error variant. */
        val LEGACY_TRANSFER_FINISHED_ERROR =
            """
            {"type":"transferFinished","payload":{
              "status":"error","errorMessage":"insufficient funds"}}
            """.trimIndent()
    }
}
