package com.app.curotel.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "curotel_prefs")

/**
 * DataStore preferences manager for app settings
 */
@Singleton
class UserPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore
    
    companion object {
        // User settings
        val IS_ONBOARDING_COMPLETE = booleanPreferencesKey("is_onboarding_complete")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        
        // Device settings
        val DEVICE_ID = stringPreferencesKey("device_id")
        val AUTO_CONNECT = booleanPreferencesKey("auto_connect")
        val LAST_CONNECTED_DEVICE = stringPreferencesKey("last_connected_device")
        
        // App settings
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val MEASUREMENT_UNIT = stringPreferencesKey("measurement_unit") // METRIC, IMPERIAL
    }
    
    // ========== Onboarding ==========
    
    val isOnboardingComplete: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[IS_ONBOARDING_COMPLETE] ?: false }
    
    suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { it[IS_ONBOARDING_COMPLETE] = complete }
    }
    
    // ========== User ==========
    
    val isLoggedIn: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[IS_LOGGED_IN] ?: false }
    
    val userName: Flow<String?> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[USER_NAME] }
    
    suspend fun setUserInfo(userId: String, name: String, email: String) {
        dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = true
            prefs[USER_ID] = userId
            prefs[USER_NAME] = name
            prefs[USER_EMAIL] = email
        }
    }
    
    suspend fun clearUserInfo() {
        dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = false
            prefs.remove(USER_ID)
            prefs.remove(USER_NAME)
            prefs.remove(USER_EMAIL)
        }
    }
    
    // ========== Device ==========
    
    val autoConnect: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[AUTO_CONNECT] ?: true }
    
    val deviceId: Flow<String?> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[DEVICE_ID] }
    
    suspend fun setAutoConnect(enabled: Boolean) {
        dataStore.edit { it[AUTO_CONNECT] = enabled }
    }
    
    suspend fun setDeviceId(id: String) {
        dataStore.edit { it[DEVICE_ID] = id }
    }
    
    // ========== Notifications ==========
    
    val notificationsEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[NOTIFICATIONS_ENABLED] ?: true }
    
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }
    
    // ========== Clear All ==========
    
    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
