package com.example.myapplication.view.presentation.addTask

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.composables.icons.lucide.File
import com.composables.icons.lucide.FileAudio
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.FileVideo
import com.composables.icons.lucide.Image
import com.composables.icons.lucide.Lucide
import com.example.myapplication.data.AttachmentInfo
import kotlinx.coroutines.launch

@Composable
fun AttachmentItem(
    attachment: AttachmentInfo,
    onDelete: () -> Unit,
    onUpdate: (AttachmentInfo) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasPermission by remember(attachment.uri) {
        mutableStateOf(checkUriPermission(context, attachment.uri))
    }

    // Add launcher for regaining permission
    val documentLauncher = rememberLauncherForActivityResult(
        contract = object : ActivityResultContracts.OpenDocument() {
            override fun createIntent(context: Context, input: Array<String>): Intent {
                val intent = super.createIntent(context, input)
                val fileName = getFileName(context, Uri.parse(attachment.uri))

                // Target the specific file if we have the name
                if (fileName != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, attachment.uri)
                    }
                    intent.putExtra(Intent.EXTRA_TITLE, fileName)
                }
                return intent
            }
        }
    ) { uri ->
        uri?.let {
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                // Refresh permission state
                hasPermission = checkUriPermission(context, uri.toString())
                if (hasPermission) {
                    val mimeType = context.contentResolver.getType(uri) ?: ""
                    val updatedAttachment = AttachmentInfo(uri = uri.toString(), type = mimeType)
                    onUpdate(updatedAttachment)
                }
            } catch (e: Exception) {
                Log.e("Permissions", "Failed to take permission: ${e.message}")
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                if (!hasPermission) {
                    // Launch document picker to regain permission
                    val mimeType = getMimeType(context, Uri.parse(attachment.uri))
                    documentLauncher.launch(arrayOf(mimeType))
                    return@clickable
                }

                scope.launch {
                    try {
                        if (!openAttachment(context, attachment.uri)) {
                            Toast
                                .makeText(
                                    context,
                                    "No app found to open this type of file",
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                        }
                    } catch (e: Exception) {
                        Log.e("Attachment", "Failed to open attachment: ${e.message}")
                        Toast
                            .makeText(
                                context,
                                "Failed to open file",
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    }
                }
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Add a visual indicator for invalid permissions
        Box(modifier = Modifier.alpha(if (hasPermission) 1f else 0.5f)) {
            if (attachment.type.startsWith("image", ignoreCase = true)) {
                ImagePreview(
                    uri = Uri.parse(attachment.uri),
                    onError = {
                        DefaultIcon(getDefaultIconForType(attachment.type))
                    }
                )
            } else {
                DefaultIcon(getDefaultIconForType(attachment.type))
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                text = remember(attachment.uri) {
                    getFileName(context, Uri.parse(attachment.uri)) ?: "Unknown File"
                }
            )
            if (!hasPermission) {
                Text(
                    text = "Access lost - tap to restore",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Attachment"
            )
        }
    }
}

@Composable
fun ImagePreview(
    uri: Uri,
    onError: @Composable () -> Unit
) {
    var isError by remember { mutableStateOf(false) }

    if (isError) {
        onError()
    } else {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri)
                .crossfade(true)
                .build(),
            contentDescription = "Image Attachment",
            modifier = Modifier.size(44.dp),
            contentScale = ContentScale.Crop,
            onError = { isError = true }
        )
    }
}

@Composable
fun DefaultIcon(iconInfo: IconInfo) {
    Icon(
        imageVector = iconInfo.icon,
        contentDescription = iconInfo.description,
        modifier = Modifier
            .size(44.dp)
            .padding(end = 8.dp),
        tint = MaterialTheme.colorScheme.onSurface
    )
}

data class IconInfo(
    val icon: ImageVector,
    val description: String,
)

fun getDefaultIconForType(mimeType: String): IconInfo {
    return when {
        mimeType.startsWith("image/") -> IconInfo(
            icon = Lucide.Image,  // Lucide icon
            description = "Image file"
        )

        mimeType.startsWith("video/") -> IconInfo(
            icon = Lucide.FileVideo,  // Lucide icon
            description = "Video file"
        )

        mimeType.startsWith("audio/") -> IconInfo(
            icon = Lucide.FileAudio,  // Lucide icon
            description = "Audio file"
        )

        mimeType.startsWith("text/") -> IconInfo(
            icon = Lucide.FileText,  // Lucide icon
            description = "Text file"
        )

        mimeType.startsWith("application/pdf") -> IconInfo(
            icon = Lucide.FileText,  // Lucide icon
            description = "PDF file"
        )

        else -> IconInfo(
            icon = Lucide.File,  // Lucide icon
            description = "File"
        )
    }
}

fun checkUriPermission(context: Context, uriString: String): Boolean {
    return try {
        val uri = Uri.parse(uriString)

        context.contentResolver.openInputStream(uri)?.use {
            return true
        } ?: false
    } catch (e: Exception) {
        Log.e("Permissions", "Failed to check URI permission: ${e.message}")
        false
    }
}


fun openAttachment(context: Context, uriString: String): Boolean {
    return try {
        val uri = Uri.parse(uriString)
        val mimeType = getMimeType(context, uri)

        // Create the intent
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // For APIs 24 and above, we need to use a FileProvider for external app access
        intent.addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)

        // Check if there's an app to handle this
        if (intent.resolveActivity(context.packageManager) != null) {
            try {
                context.startActivity(intent)
                true
            } catch (e: SecurityException) {
                Log.e("Attachment", "Security exception opening file: ${e.message}")
                false
            }
        } else {
            false
        }
    } catch (e: Exception) {
        Log.e("Attachment", "Failed to open attachment: ${e.message}")
        false
    }
}

fun getMimeType(context: Context, uri: Uri): String {
    return try {
        context.contentResolver.getType(uri) ?: "*/*"
    } catch (e: Exception) {
        Log.e("MimeType", "Failed to get mime type: ${e.message}")
        "*/*"
    }
}

fun getFileName(context: Context, uri: Uri): String? {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    cursor.getString(index)
                } else null
            } else null
        }
    } catch (e: Exception) {
        Log.e("FileName", "Failed to get file name: ${e.message}")
        null
    }
}