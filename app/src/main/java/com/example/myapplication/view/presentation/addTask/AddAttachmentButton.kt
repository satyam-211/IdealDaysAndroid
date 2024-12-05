package com.example.myapplication.view.presentation.addTask


import PermissionHandler
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
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
import com.example.myapplication.data.AttachmentInfo
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
                try {
                    // For media documents, we need to get a persistent URI
                    val finalUri =
                        getMediaContentUri(uri)


                    finalUri.let { persistableUri ->
                        // Take permission if we don't have it
                        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                        try {
                            context.contentResolver.takePersistableUriPermission(
                                persistableUri,
                                takeFlags
                            )
                        } catch (e: SecurityException) {
                            Log.w(
                                "Permissions",
                                "Could not take persistable permission: ${e.message}"
                            )
                            // Continue anyway as we might still be able to access the file
                        }

                        val mimeType = context.contentResolver.getType(persistableUri) ?: ""
                        AttachmentInfo(uri = persistableUri.toString(), type = mimeType)
                    }
                } catch (e: Exception) {
                    Log.e("Permissions", "Error handling URI permission: ${e.message}")
                    null
                }
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

fun getMediaContentUri(uri: Uri): Uri {
    try {
        if (uri.scheme == "content" && uri.authority != "com.android.providers.media.documents") {
            return uri
        }

        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":").toTypedArray()
        val type = split[0]

        return when (type.lowercase()) {
            "image" -> {
                val id = split.getOrNull(1) ?: ""
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(id).build()
            }
            "video" -> {
                val id = split.getOrNull(1) ?: ""
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(id).build()
            }
            "audio" -> {
                val id = split.getOrNull(1) ?: ""
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(id).build()
            }
            else -> uri  // Return original URI for documents
        }
    } catch (e: Exception) {
        Log.e("URI", "Error converting URI: ${e.message}")
        return uri
    }
}
