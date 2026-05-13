package io.github.kaliumlauncher.kalium.data

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

object IconLoader {
    private val semaphore = Semaphore(5)

    // Separate caches for different resolutions
    private val lowResCache = LruCache<String, Drawable>(500)
    private val medResCache = LruCache<String, Drawable>(300)
    private val highResCache = LruCache<String, Drawable>(150)

    fun getCachedLowRes(packageName: String): Drawable? = lowResCache.get(packageName)
    fun getCachedMedRes(packageName: String): Drawable? = medResCache.get(packageName)
    fun getCachedHighRes(packageName: String): Drawable? = highResCache.get(packageName)

    suspend fun loadIcon(context: Context, packageName: String, quality: IconQuality): Drawable? {
        val cache = when (quality) {
            IconQuality.LOW -> lowResCache
            IconQuality.MEDIUM -> medResCache
            IconQuality.HIGH -> highResCache
        }

        cache.get(packageName)?.let { return it }

        return semaphore.withPermit {
            cache.get(packageName)?.let { return it }

            try {
                val pm = context.packageManager
                val appInfo = pm.getApplicationInfo(packageName, 0)
                val resources = pm.getResourcesForApplication(appInfo)
                
                val iconResId = appInfo.icon
                if (iconResId == 0) return@withPermit null

                val density = when (quality) {
                    IconQuality.LOW -> DisplayMetrics.DENSITY_MEDIUM // MDPI
                    IconQuality.MEDIUM -> DisplayMetrics.DENSITY_XHIGH // XHDPI
                    IconQuality.HIGH -> DisplayMetrics.DENSITY_XXHIGH // XXHDPI TODO: Increase to higher max quality icon if screen resolution is more than 4k
                }

                // Load the icon for the specific density from the app's own resources
                val icon = resources.getDrawableForDensity(iconResId, density, null)
                if (icon != null) {
                    cache.put(packageName, icon)
                }
                icon
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Synchronous check for UI thread immediate display
     */
    fun getCachedIcon(packageName: String): Drawable? {
        return highResCache.get(packageName) 
            ?: medResCache.get(packageName) 
            ?: lowResCache.get(packageName)
    }

    suspend fun preloadIcons(context: Context, apps: List<AppInfo>) {
        withContext(Dispatchers.IO) {
            // Initial delay to let the app finish startup
            delay(2000)
            
            apps.forEach { app ->
                loadIcon(context, app.packageName, IconQuality.LOW)
                delay(10) 
            }
            
            delay(1000)

            apps.forEach { app ->
                loadIcon(context, app.packageName, IconQuality.MEDIUM)
                delay(30)
            }
        }
    }
}

enum class IconQuality {
    LOW, MEDIUM, HIGH
}
