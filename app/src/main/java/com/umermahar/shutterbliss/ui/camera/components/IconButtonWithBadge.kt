package com.umermahar.shutterbliss.ui.camera.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun IconButtonWithBadge(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    badgeCount: Int,
    onClick: () -> Unit = {}
) {

    // counter animation
    var isBlinking by remember { mutableStateOf(false) }

    // Update the text when the counter changes
    LaunchedEffect(badgeCount) {
        isBlinking = false
        delay(200)
        isBlinking = true
        delay(200) // Adjust the delay based on how many blinks you want
        isBlinking = false
        delay(200)
        isBlinking = true // Adjust the delay based on how many blinks you want
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopEnd
    ) {
        IconButton(
            onClick = onClick
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Show Content",
                tint = Color.White
            )
        }

        if (badgeCount > 0) {
            AnimatedVisibility(
                visible = isBlinking,
                enter = fadeIn(animationSpec = tween(durationMillis = 200)),
                exit = fadeOut()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(20.dp)
                        .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape),
                ) {
                    Text(
                        text = badgeCount.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = TextStyle(fontSize = 11.sp),
                    )
                }
            }
        }
    }
}