package com.umermahar.shutterbliss.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun Context.showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Context.hasPermission(permission: String): Boolean {
    return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}

fun Context.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}

fun Context.takePhoto(
    controller: LifecycleCameraController,
    onPhotoTaken: (Bitmap) -> Unit,
    onError: (String) -> Unit
) {
    controller.takePicture(
        ContextCompat.getMainExecutor(this),
        object: ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)
                val matrix = Matrix().apply {
                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                }
                val rotatedBitmap = Bitmap.createBitmap(
                    image.toBitmap(),
                    0,
                    0,
                    image.width,
                    image.height,
                    matrix,
                    true
                )
                Log.i(
                    "Actual capture picture dimension",
                    "${rotatedBitmap.width}, ${rotatedBitmap.height}"
                )
                Log.i(
                    "Actual capture picture size",
                    "${rotatedBitmap.byteCount.toFloat() / (1024f*1024f)}"
                )
//                imageSizeTesterInPng(rotatedBitmap, "original Bitmap")
//                imageSizeTesterInJpeg(rotatedBitmap, "original Bitmap")

                val resizeBitmap = rotatedBitmap.resizeBitmap(IMG_WIDTH, IMG_HEIGHT)
                Log.i(
                    "Resized capture picture dimension",
                    "${resizeBitmap.width}, ${resizeBitmap.height}"
                )
                Log.i(
                    "Resized capture picture size",
                    "${resizeBitmap.byteCount.toFloat() / (1024f*1024f)}"
                )
//                imageSizeTesterInPng(resizeBitmap, "resizeBitmap")
//                imageSizeTesterInJpeg(resizeBitmap, "resizeBitmap")
                onPhotoTaken(
                    resizeBitmap
                )

                image.close()
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                onError(exception.message.toString())
            }
        }
    )
}

fun Bitmap.resizeBitmap(targetWidth: Int, targetHeight: Int): Bitmap {
    return Bitmap.createScaledBitmap(this, targetWidth, targetHeight, true)
}

fun Context.imageSizeTesterInPng(bitmap: Bitmap, extra: String) {
    CoroutineScope(Dispatchers.Main).launch {
        withContext(Dispatchers.IO) {
            // Save the bitmap to a file and return the file
            val file = File(cacheDir, "${System.currentTimeMillis()}.png")
            try {
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

                outputStream.close()
                val fileSize = file.length().toFloat() / (1024f*1024f)
                Log.e("$extra file size png", fileSize.toString())
            } catch (e: IOException) {
                Log.e("Bitmap to file error", e.message.toString())
            }
        }
    }
}

fun Context.imageSizeTesterInJpeg(bitmap: Bitmap, extra: String) {
    CoroutineScope(Dispatchers.Main).launch {
        withContext(Dispatchers.IO) {
            // Save the bitmap to a file and return the file
            val file = File(cacheDir, "${System.currentTimeMillis()}.jpg")
            try {
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

                outputStream.close()
                val fileSize = file.length().toFloat() / (1024f * 1024f)
                Log.e("$extra file size jpeg", fileSize.toString())
            } catch (e: IOException) {
                Log.e("Bitmap to file error", e.message.toString())
            }
        }
    }
}