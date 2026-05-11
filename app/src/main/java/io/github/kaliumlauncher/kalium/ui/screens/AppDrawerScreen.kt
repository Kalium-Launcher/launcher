package io.github.kaliumlauncher.kalium.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kaliumlauncher.kalium.data.AppInfo
import io.github.kaliumlauncher.kalium.ui.components.AppItem
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme


@Composable
fun AppDrawerScreen(context: Context) {
    val apps = remember { getInstalledApps(context) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(apps) { app ->
            AppItem(app = app) {
                // Launch the app
                val intent = context.packageManager
                    .getLaunchIntentForPackage(app.packageName)
                intent?.let { context.startActivity(it) }
            }
        }
    }
}

/**
 * gets the installed apps i guess
 */
fun getInstalledApps(context: Context): List<AppInfo> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    return pm.queryIntentActivities(intent, 0)
        .map { resolveInfo ->
            AppInfo(
                name = resolveInfo.loadLabel(pm).toString(),
                packageName = resolveInfo.activityInfo.packageName,
                icon = resolveInfo.loadIcon(pm)
            )
        }
        .sortedBy { it.name } // alphabetical order
}