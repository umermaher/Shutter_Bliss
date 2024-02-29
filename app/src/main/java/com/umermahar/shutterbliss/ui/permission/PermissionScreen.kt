package com.umermahar.shutterbliss.ui.permission

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.umermahar.shutterbliss.R
import com.umermahar.shutterbliss.ui.MainActivity
import com.umermahar.shutterbliss.utils.compose.CameraPermissionTextProvider
import com.umermahar.shutterbliss.utils.compose.ObserveAsEvents
import com.umermahar.shutterbliss.utils.compose.PermissionDialog
import com.umermahar.shutterbliss.utils.hasPermission
import com.umermahar.shutterbliss.utils.openAppSettings
import com.umermahar.shutterbliss.utils.showToast
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionScreen(
    permissionDialogQueue: SnapshotStateList<String>,
    onEvent: (PermissionEvent) -> Unit,
    result: Flow<PermissionResult>,
    navigate: (route: String, currentRoute: String) -> Unit,
) {

    val activity = LocalContext.current as MainActivity

    val cameraPermissionsToRequest = remember {
        Manifest.permission.CAMERA
    }

    val cameraPermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            onEvent(
                PermissionEvent.OnPermissionResult(
                    cameraPermissionsToRequest, isGranted
                )
            )
        }
    )

    ObserveAsEvents(flow = result) { res ->
        when(res) {
            is PermissionResult.Navigate -> navigate(res.route, res.currentRoute)
        }
    }

    Scaffold (
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.app_name))
                }
            )
        }
    ) { values ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(values)
        ) {

            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.60f)
                    .padding(30.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.4f))
                Image(
                    modifier = Modifier
                        .weight(1f),
                    painter = painterResource(id = R.drawable.img_onboard_camera_pana),
                    contentDescription = "App Icon in Login Screen",
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = stringResource(R.string.add_details_to_contine),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = stringResource(R.string.camera_access_for_capture_msg),
                    textAlign = TextAlign.Center,
                )
            }

            Box (
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.40f),
                contentAlignment = Alignment.TopCenter
            ) {
                Button(
                    onClick = {
                        if(!activity.hasPermission(Manifest.permission.CAMERA))
                            cameraPermissionResultLauncher.launch(cameraPermissionsToRequest)
                        else {
                            activity.showToast(activity.getString(R.string.permission_already_granted))
                            onEvent(PermissionEvent.NavigateToCamera)
                        }
                    },
                ) {
                    Text(
                        text = stringResource(id = R.string.grant_permission),
                        modifier = Modifier
                            .padding(vertical = 8.dp),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }

    permissionDialogQueue
        .reversed()
        .forEach { permission ->
            PermissionDialog(
                permissionTextProvider = when (permission) {
                    Manifest.permission.CAMERA -> CameraPermissionTextProvider()

                    else -> return@forEach
                },
                isPermanentlyDeclined = !activity.shouldShowRequestPermissionRationale(
                    permission
                ),
                onDismiss = {
                    onEvent(PermissionEvent.DismissPermissionDialog)
                },
                onOkClick = {
                    onEvent(PermissionEvent.DismissPermissionDialog)
                    when(permission) {
                        cameraPermissionsToRequest ->
                            cameraPermissionResultLauncher.launch(permission)
                    }
                },
                onGoToAppSettingsClick = activity::openAppSettings
            )
        }
}