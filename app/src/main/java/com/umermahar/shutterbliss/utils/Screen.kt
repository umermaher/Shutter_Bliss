package com.umermahar.shutterbliss.utils

sealed class Screen(val route:String) {
    data object IntroScreen: Screen("intro_screen")
    data object CameraScreen: Screen("camera_screen")
    data object AttachmentsScreen: Screen("attachment_screen")
}