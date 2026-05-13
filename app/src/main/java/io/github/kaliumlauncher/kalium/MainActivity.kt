package io.github.kaliumlauncher.kalium

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import io.github.kaliumlauncher.kalium.data.HomeRepository
import io.github.kaliumlauncher.kalium.ui.screens.AppDrawerScreen
import io.github.kaliumlauncher.kalium.ui.screens.HomeScreen
import io.github.kaliumlauncher.kalium.ui.theme.KaliumTheme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

import androidx.compose.runtime.LaunchedEffect
import io.github.kaliumlauncher.kalium.data.IconLoader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Optimization: Initialize repository once outside setContent
        val homeRepository = HomeRepository(applicationContext)

        setContent {
            KaliumTheme {
                var showDrawer by remember { mutableStateOf(false) }

                // Preload icons in the background as soon as the app starts
                LaunchedEffect(Unit) {
                    val apps = homeRepository.getInstalledApps()
                    IconLoader.preloadIcons(applicationContext, apps)
                }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Use a Box to keep both screens in the hierarchy if needed, 
                    // or just swap them efficiently.
                    if (showDrawer) {
                        AppDrawerScreen(
                            context = LocalContext.current,
                            homeRepository = homeRepository,
                            onBack = { showDrawer = false }
                        )
                    } else {
                        HomeScreen(
                            context = LocalContext.current,
                            homeRepository = homeRepository,
                            onOpenDrawer = { showDrawer = true }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppDrawerPreview() {
    KaliumTheme {
        val context = LocalContext.current
        val homeRepository = remember { HomeRepository(context) }
        AppDrawerScreen(
            context = context,
            homeRepository = homeRepository,
            onBack = {}
        )
    }
}
