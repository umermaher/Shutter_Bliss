package com.umermahar.shutterbliss.ui.camera.components

import android.graphics.Bitmap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.umermahar.shutterbliss.R
import com.umermahar.shutterbliss.ui.theme.iconButtonColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoBottomSheetContent(
    bitmaps: List<Bitmap>,
    modifier: Modifier = Modifier,
    isSheetOpenedFromCameraScreen: Boolean = false,
    onPhotoClick: (Int) -> Unit = {},
    onDeleteButtonCLick: (Bitmap) -> Unit
) {
    if(bitmaps.isNotEmpty()) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalItemSpacing = 16.dp,
            modifier = modifier

        ) {
            itemsIndexed(bitmaps) {index, bitmap ->
                Column {
                    Box {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onPhotoClick(index)
                                }
                        )
                        if(isSheetOpenedFromCameraScreen) {
                            IconButton(
                                onClick = {
                                    onDeleteButtonCLick(bitmap)
                                },
                                modifier = Modifier
                                    .offset((-16).dp, 16.dp)
                                    .background(
                                        color = iconButtonColor,
                                        shape = CircleShape
                                    )
                                    .align(Alignment.TopEnd)
                                    .size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Attachment",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                    // if last cell has only one item than this event leads to bottom overflow means last item will be little bit cut off.
                    if(bitmaps.size % 2 != 0 && index == bitmaps.size - 1) {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    } else {
        Box(
            modifier = modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = stringResource(id = R.string.no_phots_yet))
        }
    }
}