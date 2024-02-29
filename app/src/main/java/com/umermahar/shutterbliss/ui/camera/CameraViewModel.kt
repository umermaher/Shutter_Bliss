package com.umermahar.shutterbliss.ui.camera

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(): ViewModel() {

    private val _cameraState = MutableStateFlow(CameraState())
    val cameraState = _cameraState.asStateFlow()

    private val cameraResultChannel = Channel<CameraResult>()
    val cameraResults = cameraResultChannel.receiveAsFlow()

    fun onEvent(event: CameraEvent) {
        when (event) {
            CameraEvent.OnChangeCameraSelector -> _cameraState.update {
                it.copy(
                    cameraSelector = if (it.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    } else CameraSelector.DEFAULT_FRONT_CAMERA
                )
            }

            CameraEvent.OnChangeFlashMode -> _cameraState.update {
                it.copy(
                    flashMode = when (it.flashMode) {
                        ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                        ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                        else -> ImageCapture.FLASH_MODE_OFF
                    }
                )
            }

            is CameraEvent.OnPhotoTaken -> {
                if(cameraState.value.shouldShowFrontFlash) {
                    viewModelScope.launch {
                        cameraResultChannel.send(
                            CameraResult.ResetBrightness
                        )
                    }
                }
                _cameraState.update {
                    it.copy(
                        images = it.images + event.bitmap,
                        isCameraCapturing = !it.isCameraCapturing,
                        isCapturingIconRotated = !it.isCapturingIconRotated,
                        shouldShowFrontFlash = false,
                    )
                }
            }

            is CameraEvent.TakePhoto -> {
                _cameraState.update {
                    val shouldShowFrontFlash = it.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
                            && it.flashMode == ImageCapture.FLASH_MODE_ON
                            && event.battery >= 15
                    it.copy(
                        isCameraCapturing = !it.isCameraCapturing,
                        isCapturingIconRotated = !it.isCapturingIconRotated,
                        shouldShowFrontFlash = shouldShowFrontFlash
                    )
                }
                viewModelScope.launch {
                    cameraResultChannel.send(
                        CameraResult.TakePhoto
                    )
                }
            }

            CameraEvent.OnCaptureFailed -> _cameraState.update {
                it.copy(
                    isCameraCapturing = false,
                    isCapturingIconRotated = false
                )
            }

            is CameraEvent.OnDeletePhotoClick -> {
                _cameraState.update {
                    it.copy(
                        images = it.images - event.bitmap
                    )
                }
            }
        }
    }
}