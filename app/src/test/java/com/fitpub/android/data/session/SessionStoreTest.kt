package com.fitpub.android.data.session

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionStoreTest {
    @Test
    fun normalizeServerUrl_addsSchemeAndRemovesTrailingSlashes() {
        assertEquals("https://fitpub.social", SessionStore.normalizeServerUrl("  fitpub.social/// "))
        assertEquals("http://localhost:8080", SessionStore.normalizeServerUrl("http://localhost:8080/"))
    }

    @Test
    fun normalizeServerUrl_preservesConfiguredPath() {
        assertEquals("https://example.test/fitpub", SessionStore.normalizeServerUrl("https://example.test/fitpub/"))
    }

    @Test
    fun normalizeServerUrl_returnsEmptyForBlankInput() {
        assertEquals("", SessionStore.normalizeServerUrl("  "))
    }
}
