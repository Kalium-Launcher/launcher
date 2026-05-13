package io.github.kaliumlauncher.kalium.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import io.github.kaliumlauncher.kalium.data.AppInfo
import io.github.kaliumlauncher.kalium.data.IconLoader
import io.github.kaliumlauncher.kalium.data.IconQuality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun AppItem(
    app: AppInfo,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    
    // Mipmap-style state management
    var currentIcon by remember(app.packageName) {
        val initial = IconLoader.getCachedLowRes(app.packageName)
            ?: IconLoader.getCachedMedRes(app.packageName)
            ?: IconLoader.getCachedHighRes(app.packageName)
        mutableStateOf(initial)
    }

    // Downgrade to Medium when this item leaves the viewport/composition
    DisposableEffect(app.packageName) {
        onDispose {
            val med = IconLoader.getCachedMedRes(app.packageName)
            if (med != null) {
                currentIcon = med
            }
        }
    }

    // Step 2 & 3: Progressive loading
    LaunchedEffect(app.packageName) {
        // Upgrade to LOW/MEDIUM immediately if needed
        if (currentIcon == null) {
            currentIcon = IconLoader.loadIcon(context, app.packageName, IconQuality.LOW)
        }
        
        val currentIsMed = IconLoader.getCachedMedRes(app.packageName) != null
        if (!currentIsMed) {
            val med = IconLoader.loadIcon(context, app.packageName, IconQuality.MEDIUM)
            if (med != null) currentIcon = med
        }
        
        // Wait a tiny bit more to ensure stability
        delay(150)
        
        // Only load high res if we are still in composition
        val high = IconLoader.loadIcon(context, app.packageName, IconQuality.HIGH)
        if (high != null) currentIcon = high
    }

    val painter = if (currentIcon != null) rememberDrawablePainter(currentIcon!!) else null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .drawBehind {
                    painter?.run { draw(size) }
                },
            contentAlignment = Alignment.Center
        ) {
            if (currentIcon == null) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 1.5.dp
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = app.name,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
