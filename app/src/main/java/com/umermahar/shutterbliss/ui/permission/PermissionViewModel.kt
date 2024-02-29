package com.umermahar.shutterbliss.ui.permission

import android.Manifest
import android.os.Build
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umermahar.shutterbliss.ui.camera.CameraResult
import com.umermahar.shutterbliss.utils.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(): ViewModel() {
    val permissionDialogQueue = mutableStateListOf<String>()

    private val resultChannel = Channel<PermissionResult>()
    val permissionResults = resultChannel.receiveAsFlow()

    fun onEvent(event: PermissionEvent) {
        when(event) {
            is PermissionEvent.OnPermissionResult -> onPermissionResult(event.permission, event.isGranted)

            PermissionEvent.DismissPermissionDialog -> permissionDialogQueue.removeFirst()
            PermissionEvent.NavigateToCamera -> viewModelScope.launch {
                resultChannel.send(
                    PermissionResult.Navigate(
                        route = Screen.CameraScreen.route,
                        currentRoute = Screen.IntroScreen.route
                    )
                )
            }
        }
    }

    private fun onPermissionResult(
        permission: String,
        isGranted: Boolean
    ) {
        if(!isGranted && !permissionDialogQueue.contains(permission)) {
            permissionDialogQueue.add(permission)
        } else if(isGranted && permission == Manifest.permission.CAMERA) {
            onEvent(PermissionEvent.NavigateToCamera)
        }
    }

}

sealed interface PermissionEvent {

    data class OnPermissionResult(
        val permission: String,
        val isGranted: Boolean
    ): PermissionEvent

    data object DismissPermissionDialog: PermissionEvent

    data object NavigateToCamera: PermissionEvent
}

sealed interface PermissionResult {
    data class Navigate(val route : String, val currentRoute: String): PermissionResult
}
