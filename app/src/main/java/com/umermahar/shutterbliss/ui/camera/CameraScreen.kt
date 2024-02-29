package com.umermahar.shutterbliss.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.os.BatteryManager
import android.util.Log
import android.view.KeyEvent
import android.view.Window
import android.view.WindowManager
import androidx.camera.core.ImageCapture
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BlurCircular
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.umermahar.shutterbliss.ui.MainActivity
import com.umermahar.shutterbliss.ui.camera.components.CameraPreview
import com.umermahar.shutterbliss.ui.camera.components.IconButtonWithBadge
import com.umermahar.shutterbliss.ui.camera.components.PhotoBottomSheetContent
import com.umermahar.shutterbliss.ui.theme.iconButtonColor
import com.umermahar.shutterbliss.ui.theme.iconButtonWithBadgeColor
import com.umermahar.shutterbliss.utils.compose.ObserveAsEvents
import com.umermahar.shutterbliss.utils.takePhoto
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    state: CameraState,
    onEvent: (CameraEvent) -> Unit,
    results: Flow<CameraResult>,
    onPhotoClick: (Int, List<Bitmap>) -> Unit,
) {
    val activity = LocalContext.current as MainActivity
    val window: Window = activity.window
    val params: WindowManager.LayoutParams = window.attributes
    val lastScreenBrightness = params.screenBrightness

    val batteryManager = activity.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val controller = remember {
        LifecycleCameraController(activity).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        }
    }

    // focus on the box to listen key events i.e: Volume Button to capture image
    val focusRequester = remember { FocusRequester() }

    // Due to multiple compositions onEvent callback for key events can triggers more than one time.
    var isHardwareKeyEventHandled by remember { mutableStateOf(false) }

    // Define the target rotation values
    val capturingIconRotation = if (state.isCapturingIconRotated) 0f else 360f

    // Use animateFloatAsState to animate the rotation
    val rotationState by animateFloatAsState(
        targetValue = capturingIconRotation,
        animationSpec = tween(durationMillis = 1000),
        label = "CapturingIconRotationTransition"
    )

    ObserveAsEvents(flow = results) { res ->
        when (res) {
            CameraResult.TakePhoto -> {
                activity.takePhoto(
                    controller = controller,
                    onPhotoTaken = {
                        onEvent(
                            CameraEvent.OnPhotoTaken(bitmap = it)
                        )
                    },
                    onError = { msg ->
                        Log.e("Camera Error!", msg)
                        onEvent(CameraEvent.OnCaptureFailed)
                    }
                )
            }

            CameraResult.ResetBrightness -> {
                params.screenBrightness = lastScreenBrightness
                window.attributes = params
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(state) {
        controller.cameraSelector = state.cameraSelector
        controller.imageCaptureFlashMode = state.flashMode
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            PhotoBottomSheetContent(
                bitmaps = state.images,
                modifier = Modifier.fillMaxWidth()
                    .padding(16.dp),
                isSheetOpenedFromCameraScreen = true,
                onPhotoClick = { currentAttachmentIndex ->
                    onPhotoClick(currentAttachmentIndex, state.images)
                },
                onDeleteButtonCLick = {
                    onEvent(
                        CameraEvent.OnDeletePhotoClick(bitmap = it)
                    )
                }
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onKeyEvent {
                    // Utilizing hardware keys
                    // Volume buttons for capturing
                    if (!isHardwareKeyEventHandled && !state.isCameraCapturing) {
                        when (it.nativeKeyEvent.keyCode) {
                            KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP -> {
                                val battery =
                                    batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                                onEvent(
                                    CameraEvent.TakePhoto(battery = battery)
                                )
                            }

                            KeyEvent.KEYCODE_BACK -> activity.onBackPressedDispatcher.onBackPressed()
                        }
                        // Set the flag to ignore further events for now
                        isHardwareKeyEventHandled = true
                        // Reset the flag after a delay to allow more events
                        // Adjust the delay duration as needed
                        scope.launch {
                            delay(1000L)
                            isHardwareKeyEventHandled = false
                        }
                    }
                    true
                }
                .focusRequester(focusRequester)
                .focusable()
                .padding(paddingValues)
        ) {
            CameraPreview(
                controller = controller,
                modifier = Modifier
                    .fillMaxSize()
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset((-16).dp, 16.dp)
            ) {
                IconButton(
                    onClick = {
                        onEvent(CameraEvent.OnChangeCameraSelector)
                    },
                    modifier = Modifier
                        .background(
                            color = iconButtonColor,
                            shape = CircleShape
                        )
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))
                IconButton(
                    onClick = {
                        onEvent(CameraEvent.OnChangeFlashMode)
                    },
                    modifier = Modifier
                        .background(
                            color = iconButtonColor,
                            shape = CircleShape
                        )
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = when(state.flashMode) {
                            ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                            ImageCapture.FLASH_MODE_OFF -> Icons.Default.FlashOff
                            else -> Icons.Default.FlashAuto
                        },
                        contentDescription = "Switch Camera",
                        tint = Color.White
                    )
                }
                // Muted
//                Spacer(modifier = Modifier.width(10.dp))
//                IconButton(
//                    onClick = {
//                        navController.navigate(Screen.LandmarkRecognitionScreen.route)
//                    },
//                    modifier = Modifier
//                        .background(
//                            color = textParagraphColor.copy(alpha = 0.4f),
//                            shape = CircleShape
//                        )
//                        .size(40.dp)
//                ) {
//                    Icon(
//                        modifier = Modifier.padding(5.dp),
//                        painter = painterResource(id = R.drawable.ic_landmark),
//                        contentDescription = "Landmark Recognition",
//                        tint = Color.White
//                    )
//                }
            }

            IconButton(
                onClick = {
                    val battery = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                    onEvent(
                        CameraEvent.TakePhoto(battery = battery)
                    )
                } ,
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-24).dp)
            ) {
                Icon(
                    modifier = Modifier
                        .size(80.dp),
                    imageVector = Icons.Default.BlurCircular,
                    contentDescription = "Take Photo",
                    tint = Color.White
                )
            }

            if (state.images.isNotEmpty())
                IconButtonWithBadge(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(16.dp, (-40).dp)
                        .background(
                            color = iconButtonWithBadgeColor,
                            shape = CircleShape
                        )
                        .clickable {
                            scope.launch {
                                scaffoldState.bottomSheetState.expand()
                            }
                        },
                    icon = Icons.Default.Photo,
                    badgeCount = state.images.size,
                    onClick = {
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    }
                )

            if(state.isCameraCapturing) {
                // Rotating icon while capturing
                Icon(
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.Center)
                        .rotate(rotationState),
                    imageVector = Icons.Default.Camera,
                    contentDescription = "Take Photo",
                    tint = Color.White
                )
            }

            AnimatedVisibility(
                visible = state.isCameraCapturing,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.White.copy(alpha = 0.2f))
                )
            }
        }
    }

    if(state.shouldShowFrontFlash) {
        var color: Int = android.graphics.Color.WHITE
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color, hsv)
        hsv[2] *= 1f
        color = android.graphics.Color.HSVToColor(hsv)
        params.screenBrightness = 1F
        window.attributes = params
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(color))
        )
    }
}