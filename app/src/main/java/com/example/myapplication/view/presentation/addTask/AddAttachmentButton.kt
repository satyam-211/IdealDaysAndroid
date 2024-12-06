package com.example.myapplication.view.presentation.addTask


import PermissionHandler
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.AttachmentInfo
import com.example.myapplication.utils.Utils
import kotlinx.coroutines.launch

@Composable
fun AddAttachmentButton(
    onAttachmentsSelected: (List<AttachmentInfo>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        arrayOf(
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }
    var showRationale by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    // Launcher for opening the document
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri>? ->
        uris?.let { selectedUris ->
            val attachments = selectedUris.mapNotNull { uri ->
                Utils.getPersistentAttachmentInfo(uri, context)
            }

            if (attachments.isNotEmpty()) {
                onAttachmentsSelected(attachments)
            }
        }
    }


    // Launcher for requesting READ_EXTERNAL_STORAGE permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) {
            filePickerLauncher.launch(arrayOf("*/*"))
        } else {
            showRationale = true
        }
    }

    // Using AlertDialog instead of Toast for better UX
    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Permissions Required") },
            text = { Text("Storage access permissions are required to add attachments. Please grant them in Settings.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                        // Open app settings
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        })
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) {
                    Text("Cancel")
                }
            }
        )
    }



    Button(
        onClick = {
            if (PermissionHandler.hasAllAttachmentPermissions(
                    context = context,
                    permissions = permissions
                )
            ) {
                filePickerLauncher.launch(arrayOf("*/*"))
            } else {
                coroutineScope.launch {
                    permissionLauncher.launch(permissions)
                }
            }
        },
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.AddCircle,
                contentDescription = "Add Attachment"
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Add Attachments", style = MaterialTheme.typography.bodySmall)
        }
    }
}


