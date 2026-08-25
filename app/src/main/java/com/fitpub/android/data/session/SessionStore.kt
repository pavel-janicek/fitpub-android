package com.fitpub.android.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Immutable snapshot of the session the app currently holds. The [serverUrl] is the
 * instance base URL (with trailing slash stripped) that every API call is routed to,
 * which is what lets a single app talk to any FitPub instance (federation friendly).
 */
data class Session(
    val serverUrl: String = "",
    val token: String = "",
    val username: String = "",
    val displayName: String = "",
    val email: String = "",
) {
    val isLoggedIn: Boolean get() = token.isNotBlank()
    val isConfigured: Boolean get() = serverUrl.isNotBlank()
}

private val Context.fitPubDataStore: DataStore<Preferences> by preferencesDataStore(name = "fitpub_session")

/**
 * Persists the currently selected FitPub instance and (if logged in) the JWT bearer
 * token plus cached identity fields. Tokens are held in plain app-private storage;
 * consider encrypting with EncryptedSharedPreferences in a hardened build.
 */
class SessionStore(private val context: Context) {

    private object Keys {
        val SERVER_URL = stringPreferencesKey("server_url")
        val TOKEN = stringPreferencesKey("token")
        val USERNAME = stringPreferencesKey("username")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val EMAIL = stringPreferencesKey("email")
    }

    val session: Flow<Session> = context.fitPubDataStore.data.map { prefs ->
        Session(
            serverUrl = prefs[Keys.SERVER_URL] ?: "",
            token = prefs[Keys.TOKEN] ?: "",
            username = prefs[Keys.USERNAME] ?: "",
            displayName = prefs[Keys.DISPLAY_NAME] ?: "",
            email = prefs[Keys.EMAIL] ?: "",
        )
    }

    suspend fun setServerUrl(rawUrl: String) {
        val normalized = normalizeServerUrl(rawUrl)
        context.fitPubDataStore.edit { it[Keys.SERVER_URL] = normalized }
    }

    suspend fun saveAuth(
        token: String,
        username: String,
        displayName: String?,
        email: String?,
    ) {
        context.fitPubDataStore.edit {
            it[Keys.TOKEN] = token
            it[Keys.USERNAME] = username
            it[Keys.DISPLAY_NAME] = displayName.orEmpty()
            it[Keys.EMAIL] = email.orEmpty()
        }
    }

    suspend fun updateDisplayName(displayName: String) {
        context.fitPubDataStore.edit { it[Keys.DISPLAY_NAME] = displayName }
    }

    suspend fun logout() {
        context.fitPubDataStore.edit {
            it.remove(Keys.TOKEN)
            it.remove(Keys.USERNAME)
            it.remove(Keys.DISPLAY_NAME)
            it.remove(Keys.EMAIL)
        }
    }

    companion object {
        /** Normalizes a user-entered instance URL to a canonical base (no trailing slash). */
        fun normalizeServerUrl(raw: String): String {
            var url = raw.trim()
            if (url.isBlank()) return ""
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://$url"
            }
            // Strip a well-known path suffix so "https://fitpub.social/" -> "https://fitpub.social"
            while (url.endsWith("/")) url = url.dropLast(1)
            return url
        }
    }
}