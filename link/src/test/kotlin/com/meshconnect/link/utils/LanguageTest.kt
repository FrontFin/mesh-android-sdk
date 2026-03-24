package com.meshconnect.link.utils

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Locale

class LanguageTest {
    private lateinit var originalLocale: Locale

    @Before
    fun setUp() {
        originalLocale = Locale.getDefault()
    }

    @After
    fun tearDown() {
        Locale.setDefault(originalLocale)
    }

    // systemLanguage

    @Test
    fun `systemLanguage should return BCP 47 tag for English US`() {
        Locale.setDefault(Locale.US)
        systemLanguage shouldBeEqualTo "en-US"
    }

    @Test
    fun `systemLanguage should return BCP 47 tag for French France`() {
        Locale.setDefault(Locale.FRANCE)
        systemLanguage shouldBeEqualTo "fr-FR"
    }

    @Test
    fun `systemLanguage should return BCP 47 tag for Japanese`() {
        Locale.setDefault(Locale.JAPAN)
        systemLanguage shouldBeEqualTo "ja-JP"
    }

    @Test
    fun `systemLanguage should return BCP 47 tag for German`() {
        Locale.setDefault(Locale.GERMANY)
        systemLanguage shouldBeEqualTo "de-DE"
    }

    @Test
    fun `systemLanguage should return BCP 47 tag for Chinese Simplified`() {
        Locale.setDefault(Locale.SIMPLIFIED_CHINESE)
        systemLanguage shouldBeEqualTo "zh-CN"
    }

    @Test
    fun `systemLanguage should reflect locale changes at runtime`() {
        Locale.setDefault(Locale.US)
        systemLanguage shouldBeEqualTo "en-US"

        Locale.setDefault(Locale.FRANCE)
        systemLanguage shouldBeEqualTo "fr-FR"
    }

    // resolveLanguage

    @Test
    fun `resolveLanguage should return system language when value is system`() {
        Locale.setDefault(Locale.US)
        resolveLanguage("system") shouldBeEqualTo "en-US"
    }

    @Test
    fun `resolveLanguage should return passed language when value is a language code`() {
        resolveLanguage("fr-FR") shouldBeEqualTo "fr-FR"
    }

    @Test
    fun `resolveLanguage should return null when value is null`() {
        resolveLanguage(null).shouldBeNull()
    }

    @Test
    fun `resolveLanguage should return empty string when value is empty`() {
        resolveLanguage("") shouldBeEqualTo ""
    }
}
