package io.github.kaliumlauncher.kalium.ui.screens

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import io.github.kaliumlauncher.kalium.data.AppInfo
import io.github.kaliumlauncher.kalium.data.HomeRepository
import io.github.kaliumlauncher.kalium.ui.components.AppItem
import io.github.kaliumlauncher.kalium.ui.components.LongPressContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    context: Context,
    homeRepository: HomeRepository,
    onOpenDrawer: () -> Unit
) {
    val homeAppPackages by homeRepository.homeAppsFlow.collectAsState(initial = emptySet())
    
    // Load apps asynchronously to avoid blocking the UI thread
    val allApps by produceState<List<AppInfo>?>(initialValue = null, homeRepository) {
        value = homeRepository.getInstalledApps()
    }

    val homeApps = remember(allApps, homeAppPackages) {
        allApps?.filter { it.packageName in homeAppPackages }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    var totalDrag = 0f
                    
                    do {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        val dragEvent = event.changes[0]
                        if (dragEvent.pressed) {
                            val dragAmount = dragEvent.position.y - dragEvent.previousPosition.y
                            totalDrag += dragAmount
                            
                            if (totalDrag < -20) {
                                dragEvent.consume()
                            }
                        }
                    } while (event.changes.fastAny { it.pressed })

                    if (totalDrag < -100) {
                        onOpenDrawer()
                    }
                }
            }
    ) {
        if (homeApps == null) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                items(homeApps, key = { it.packageName }) { app ->
                    var showMenu by remember { mutableStateOf(false) }
                    
                    Box {
                        AppItem(
                            app = app,
                            modifier = Modifier.combinedClickable(
                                onClick = {
                                    val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                    intent?.let { context.startActivity(it) }
                                },
                                onLongClick = {
                                    showMenu = true
                                }
                            )
                        )
                        
                        if (showMenu) {
                            LongPressContext(
                                app = app,
                                homeRepository = homeRepository,
                                isHomeScreen = true,
                                onDismiss = { showMenu = false }
                            )
                        }
                    }
                }
            }
        }

        // Button to open drawer
        Button(
            onClick = onOpenDrawer,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
        ) {
            Text("Apps")
        }
    }
}
