package io.github.kaliumlauncher.kalium.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.kaliumlauncher.kalium.data.AppInfo
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kaliumlauncher.kalium.R
import io.github.kaliumlauncher.kalium.data.HomeRepository
import io.github.kaliumlauncher.kalium.ui.theme.KaliumTheme
import kotlinx.coroutines.launch

@Composable
fun LongPressContext(
    app: AppInfo,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    homeRepository: HomeRepository,
    isHomeScreen: Boolean = false
) {
    val scope = rememberCoroutineScope()
    LongPressContextContent(
        app = app,
        onDismiss = onDismiss,
        onAddAppToHome = {
            scope.launch {
                homeRepository.addAppToHome(app.packageName)
                onDismiss()
            }
        },
        onRemoveAppFromHome = {
            scope.launch {
                homeRepository.removeAppFromHome(app.packageName)
                onDismiss()
            }
        },
        isHomeScreen = isHomeScreen,
        modifier = modifier
    )
}

@Composable
fun LongPressContextContent(
    app: AppInfo,
    onDismiss: () -> Unit,
    onAddAppToHome: () -> Unit,
    onRemoveAppFromHome: () -> Unit,
    modifier: Modifier = Modifier,
    isHomeScreen: Boolean = false
) {
    val context = LocalContext.current
    Card(
        onDismiss,
        modifier = modifier
    ) {
        Card(
            onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", app.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                onDismiss()
            },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),

        ) {
            Icon(
                painter = painterResource(R.drawable.info_24),
                contentDescription = "App Info"
            )
        }
    }
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        DropdownMenuItem(
            text = { Text("App info") },
            onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", app.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text("Uninstall") },
            onClick = {
                val intent = Intent(Intent.ACTION_DELETE).apply {
                    data = Uri.parse("package:${app.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                onDismiss()
            }
        )
        if (isHomeScreen) {
            DropdownMenuItem(
                text = { Text("Remove from home") },
                onClick = onRemoveAppFromHome
            )
        } else {
            DropdownMenuItem(
                text = { Text("Add to home") },
                onClick = onAddAppToHome
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LongPressContextPreview() {
    KaliumTheme {
        Box(modifier = Modifier.size(200.dp)) {
            LongPressContextContent(
                app = AppInfo("Calculator", "com.android.calculator2"),
                onDismiss = {},
                onAddAppToHome = {},
                onRemoveAppFromHome = {},
                isHomeScreen = false
            )
        }
    }
}
