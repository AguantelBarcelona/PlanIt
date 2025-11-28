package com.tuempresa.PlanIt.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(context: Context) {

    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val LOGGED_IN_USER = stringPreferencesKey("logged_in_user")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
    }

    val loggedInUser: Flow<String?> = dataStore.data.map {
        it[PreferencesKeys.LOGGED_IN_USER]
    }

    val isDarkTheme: Flow<Boolean> = dataStore.data.map {
        it[PreferencesKeys.IS_DARK_THEME] ?: false
    }

    suspend fun saveUserSession(username: String) {
        dataStore.edit {
            it[PreferencesKeys.LOGGED_IN_USER] = username
        }
    }

    suspend fun clearUserSession() {
        dataStore.edit {
            it.remove(PreferencesKeys.LOGGED_IN_USER)
        }
    }

    suspend fun setTheme(isDark: Boolean) {
        dataStore.edit {
            it[PreferencesKeys.IS_DARK_THEME] = isDark
        }
    }
}