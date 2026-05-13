package io.github.kaliumlauncher.kalium.data

import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.dataStore by preferencesDataStore(name = "home_layout")

class HomeRepository(private val context: Context) {
    private val HOME_APPS_KEY = stringSetPreferencesKey("home_apps")
    
    // Cache the installed apps list in memory to speed up navigation
    private var cachedApps: List<AppInfo>? = null

    val homeAppsFlow: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[HOME_APPS_KEY] ?: emptySet()
        }

    suspend fun addAppToHome(packageName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[HOME_APPS_KEY] ?: emptySet()
            preferences[HOME_APPS_KEY] = current + packageName
        }
    }

    suspend fun removeAppFromHome(packageName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[HOME_APPS_KEY] ?: emptySet()
            preferences[HOME_APPS_KEY] = current - packageName
        }
    }

    /**
     * gets the installed apps - optimized with in-memory cache
     */
    suspend fun getInstalledApps(forceRefresh: Boolean = false): List<AppInfo> {
        if (!forceRefresh && cachedApps != null) {
            return cachedApps!!
        }
        
        return withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val apps = pm.queryIntentActivities(intent, 0)
                .map { resolveInfo ->
                    AppInfo(
                        name = resolveInfo.loadLabel(pm).toString(),
                        packageName = resolveInfo.activityInfo.packageName
                    )
                }
                .sortedBy { it.name }
            
            cachedApps = apps
            apps
        }
    }
}
