package com.umermahar.shutterbliss.ui.camera.attachment

import android.graphics.Bitmap
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.umermahar.shutterbliss.ui.theme.swipeImageIconColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AttachmentsScreen(
    currentAttachmentIndex: Int,
    attachments: List<Bitmap>,
    popBackStack: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = currentAttachmentIndex,
    ) {
        // provide pageCount
        attachments.size
    }
    val scope = rememberCoroutineScope()

    var rotationState by remember { mutableFloatStateOf(0f) }

    val rotation by animateFloatAsState(
        targetValue = rotationState,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "Image Animation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()

    ) {

        IconButton(
            onClick = popBackStack,
            modifier = Modifier
                .offset(16.dp, 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Go Back",
            )
        }

        IconButton(
            onClick = {
                rotationState += 90f
            },
            modifier = Modifier
                .offset((-16).dp, 16.dp)
                .align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.RotateRight,
                contentDescription = "Go Back",
            )
        }

        HorizontalPager(
            state = pagerState,
            key = { it },
            modifier = Modifier.align(Alignment.Center)
        ) { index ->
            Box {
                Image(
                    bitmap = attachments[index].asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            rotationZ = rotation
                        }
                )
            }
        }

        if (attachments.size != 1) {
            Box(
                modifier = Modifier
                    .offset(y = -(16.dp))
                    .fillMaxWidth(0.5f)
                    .clip(RoundedCornerShape(100))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(8.dp)
                    .align(Alignment.BottomCenter)
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage - 1
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .alpha(
                            if (pagerState.currentPage != 0) {
                                1f
                            } else 0.5f
                        ),
                    enabled = pagerState.currentPage != 0,
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Go back",
                        tint = swipeImageIconColor
                    )
                }
                IconButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage + 1
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .alpha(
                            if (pagerState.currentPage != attachments.size - 1) {
                                1f
                            } else 0.5f
                        ),
                    enabled = pagerState.currentPage != attachments.size - 1,
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Go Forward",
                        tint = swipeImageIconColor
                    )
                }
            }
        }
    }
}