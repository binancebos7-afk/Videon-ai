package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.studioDataStore: DataStore<Preferences> by preferencesDataStore(name = "videon_prefs")

data class StudioPreferences(
    val isDarkMode: Boolean = true,
    val language: AppLanguage = AppLanguage.ID,
    val selfHostedEndpoint: String = "",
    val userDisplayName: String = "Studio Creator",
    val defaultFps: Int = 24,
    val autosaveEnabled: Boolean = true
)

class PreferencesRepository(private val context: Context) {
    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val SELF_HOSTED_URL = stringPreferencesKey("self_hosted_url")
        val USER_NAME = stringPreferencesKey("user_name")
        val DEFAULT_FPS = intPreferencesKey("default_fps")
        val AUTOSAVE = booleanPreferencesKey("autosave")
    }

    val preferences: Flow<StudioPreferences> = context.studioDataStore.data.map { prefs ->
        StudioPreferences(
            isDarkMode = prefs[Keys.DARK_MODE] ?: true,
            language = if (prefs[Keys.LANGUAGE] == "en") AppLanguage.EN else AppLanguage.ID,
            selfHostedEndpoint = prefs[Keys.SELF_HOSTED_URL] ?: "",
            userDisplayName = prefs[Keys.USER_NAME] ?: "Studio Creator",
            defaultFps = prefs[Keys.DEFAULT_FPS] ?: 24,
            autosaveEnabled = prefs[Keys.AUTOSAVE] ?: true
        )
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.studioDataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.studioDataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun setSelfHostedEndpoint(url: String) {
        context.studioDataStore.edit { it[Keys.SELF_HOSTED_URL] = url.trim() }
    }

    suspend fun setUserDisplayName(name: String) {
        context.studioDataStore.edit { it[Keys.USER_NAME] = name.trim().ifBlank { "Studio Creator" } }
    }

    suspend fun setDefaultFps(fps: Int) {
        context.studioDataStore.edit { it[Keys.DEFAULT_FPS] = fps }
    }

    suspend fun setAutosaveEnabled(enabled: Boolean) {
        context.studioDataStore.edit { it[Keys.AUTOSAVE] = enabled }
    }
}
