package com.meshconnect.link.ui

import android.net.Uri
import android.os.Looper
import android.webkit.WebView
import androidx.webkit.JavaScriptReplyProxy
import androidx.webkit.WebMessageCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.meshconnect.link.R
import com.meshconnect.link.entity.LinkConfiguration
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LinkActivityBridgeTest {
    private val linkOrigin = "https://link.meshconnect.com"
    private val closeMessage = "{\"type\":\"close\"}"
    private val listener = slot<WebViewCompat.WebMessageListener>()

    @Before
    fun setUp() {
        mockkStatic(WebViewFeature::class, WebViewCompat::class)
        every { WebViewCompat.addWebMessageListener(any(), any(), any(), capture(listener)) } returns Unit
        every { WebViewCompat.addDocumentStartJavaScript(any(), any(), any()) } returns mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkStatic(WebViewFeature::class, WebViewCompat::class)
    }

    @Test
    fun `new WebViews limit the bridge to the Link origin from the token`() {
        supportFeatures(true)
        val activity = launch().get()
        val webView = activity.findViewById<WebView>(R.id.web_view)

        verify { WebViewCompat.addWebMessageListener(webView, JSBridge.NAME, setOf(linkOrigin), any()) }
        verify { WebViewCompat.addDocumentStartJavaScript(webView, JSBridge.SEND_NATIVE_MESSAGE_SCRIPT, setOf(linkOrigin)) }
        assertNull(shadowOf(webView).getJavascriptInterface(JSBridge.NAME))
    }

    @Test
    fun `new WebViews accept messages from the Link page`() {
        supportFeatures(true)
        val activity = launch().get()

        sendFromWebView(isMainFrame = true)

        assertTrue(waitUntil { activity.isFinishing })
    }

    @Test
    fun `new WebViews drop messages from iframes`() {
        supportFeatures(true)
        val activity = launch().get()

        sendFromWebView(isMainFrame = false)

        assertFalse(waitUntil { activity.isFinishing })
    }

    @Test
    fun `new WebViews ignore non-string messages without crashing`() {
        supportFeatures(true)
        val activity = launch().get()

        sendFromWebView(isMainFrame = true, message = WebMessageCompat(byteArrayOf(1)))

        assertFalse(waitUntil { activity.isFinishing })
    }

    @Test
    fun `old WebViews fall back to the JavaScript interface`() {
        supportFeatures(false)
        val activity = launch().get()
        val webView = activity.findViewById<WebView>(R.id.web_view)

        val bridge = shadowOf(webView).getJavascriptInterface(JSBridge.NAME)
        assertNotNull(bridge)
        assertEquals(JSBridge::class.java, bridge.javaClass)
        verify(exactly = 0) { WebViewCompat.addWebMessageListener(any(), any(), any(), any()) }

        (bridge as JSBridge).sendNativeMessage(closeMessage)

        assertTrue(waitUntil { activity.isFinishing })
    }

    private fun supportFeatures(supported: Boolean) {
        every { WebViewFeature.isFeatureSupported(any()) } returns supported
    }

    private fun launch(): ActivityController<LinkActivity> {
        val config = LinkConfiguration(token = "$linkOrigin/?token=abc")
        val intent = LinkActivity.getLinkIntent(RuntimeEnvironment.getApplication(), config)
        return Robolectric.buildActivity(LinkActivity::class.java, intent).setup()
    }

    private fun sendFromWebView(
        isMainFrame: Boolean,
        message: WebMessageCompat = WebMessageCompat(closeMessage),
    ) {
        listener.captured.onPostMessage(
            mockk<WebView>(),
            message,
            Uri.parse(linkOrigin),
            isMainFrame,
            mockk<JavaScriptReplyProxy>(),
        )
    }

    // The ViewModel parses messages on a background thread, then posts the result
    // to the main thread, so run the main looper until the check passes or time is up.
    private fun waitUntil(check: () -> Boolean): Boolean {
        repeat(WAIT_STEPS) {
            shadowOf(Looper.getMainLooper()).idle()
            if (check()) return true
            Thread.sleep(WAIT_STEP_MS)
        }
        return false
    }

    private companion object {
        const val WAIT_STEPS = 20
        const val WAIT_STEP_MS = 50L
    }
}
