package com.umermahar.shutterbliss.ui.camera

import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture

data class CameraState(
    val images: List<Bitmap> = emptyList(),
    val cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA, // current camera selector
    val flashMode: Int = ImageCapture.FLASH_MODE_OFF, // current camera flash mode
    val isCapturingIconRotated: Boolean = false, // Animated Icon rotating state
    val isCameraCapturing: Boolean = false,
    val shouldShowFrontFlash: Boolean = false
)

sealed class CameraResult {
    object TakePhoto: CameraResult()
    object ResetBrightness: CameraResult()
}

sealed interface CameraEvent {
    object OnChangeCameraSelector: CameraEvent
    object OnChangeFlashMode: CameraEvent
    object OnCaptureFailed: CameraEvent
    data class TakePhoto(val battery: Int):  CameraEvent
    data class OnPhotoTaken(val bitmap: Bitmap): CameraEvent
    data class OnDeletePhotoClick(val bitmap: Bitmap): CameraEvent
}