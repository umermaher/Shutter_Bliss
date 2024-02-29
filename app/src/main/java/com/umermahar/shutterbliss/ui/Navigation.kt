package com.umermahar.shutterbliss.ui

import android.Manifest
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.umermahar.shutterbliss.ui.camera.CameraScreen
import com.umermahar.shutterbliss.ui.camera.CameraViewModel
import com.umermahar.shutterbliss.ui.camera.attachment.AttachmentsScreen
import com.umermahar.shutterbliss.ui.permission.PermissionScreen
import com.umermahar.shutterbliss.ui.permission.PermissionViewModel
import com.umermahar.shutterbliss.utils.ATTACHMENTS
import com.umermahar.shutterbliss.utils.CURRENT_ATTACHMENT_INDEX
import com.umermahar.shutterbliss.utils.Screen
import com.umermahar.shutterbliss.utils.hasPermission

@Composable
fun Navigation() {

    val context = LocalContext.current
    val rootNavController = rememberNavController()

    NavHost(
        navController = rootNavController,
        startDestination =
        if (context.hasPermission(Manifest.permission.CAMERA)) Screen.CameraScreen.route
        else Screen.IntroScreen.route
    ) {

        composable(route = Screen.IntroScreen.route) {
            val viewModel: PermissionViewModel = hiltViewModel()
            PermissionScreen(
                permissionDialogQueue = viewModel.permissionDialogQueue,
                onEvent = viewModel::onEvent,
                result = viewModel.permissionResults,
                navigate = { route, currentRoute ->
                    rootNavController.navigate(route) {
                        popUpTo(currentRoute) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(route = Screen.CameraScreen.route) {
            val viewModel: CameraViewModel = hiltViewModel()
            val state by viewModel.cameraState.collectAsStateWithLifecycle()
            // Attachments to render on Horizontal pager
            CameraScreen(
                state = state,
                onEvent = viewModel::onEvent,
                results = viewModel.cameraResults,
                onPhotoClick = { currentPhotoIndex: Int, atts: List<Bitmap> ->
                    rootNavController.navigate(
                        route = Screen.AttachmentsScreen.route + "/${currentPhotoIndex}/${Gson().toJson(atts)}"
                    )
                },
            )
        }

        composable(
            route = Screen.AttachmentsScreen.route + "/{$CURRENT_ATTACHMENT_INDEX}/{$ATTACHMENTS}",
            arguments = listOf(
                navArgument(CURRENT_ATTACHMENT_INDEX) {
                    type = NavType.IntType
                },
                navArgument(ATTACHMENTS) {
                    type = NavType.StringType
                }
            )
        ) {
            val param = it.arguments?.getString(ATTACHMENTS) ?: ""
            val currentAttachmentIndex = it.arguments?.getInt(CURRENT_ATTACHMENT_INDEX) ?: 0
            val listType = object : TypeToken<List<Bitmap>>() {}.type
            val attachments = Gson().fromJson(param, listType) as List<Bitmap>
            AttachmentsScreen(
                currentAttachmentIndex = currentAttachmentIndex,
                attachments = attachments,
                popBackStack = {
                    rootNavController.popBackStack()
                }
            )
        }
    }
}