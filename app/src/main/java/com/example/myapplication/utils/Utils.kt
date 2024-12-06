package com.example.myapplication.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.myapplication.model.AttachmentInfo
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object Utils {

    fun formatTime(millis: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun getPersistentAttachmentInfo(
        uri: Uri,
        context: Context
    ) = try {
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
            }

            val mimeType = context.contentResolver.getType(persistableUri) ?: ""
            AttachmentInfo(uri = persistableUri, type = mimeType)
        }
    } catch (e: Exception) {
        Log.e("Permissions", "Error handling URI permission: ${e.message}")
        null
    }

    private fun getMediaContentUri(uri: Uri): Uri {
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
}

fun LocalDate.Companion.now(): LocalDate {
    val currentInstant = Clock.System.now()
    return currentInstant.toLocalDateTime(TimeZone.currentSystemDefault()).date
}

/**
 * Extension function to format kotlinx.datetime.LocalDate to a string like "14th Sept 2024".
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.formatWithOrdinal(): String {
    val day = this.dayOfMonth
    val suffix = getDayOfMonthSuffix(day)
    val month =
        this.toJavaLocalDate().format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault()))
    val year = this.year
    return "$day$suffix $month $year"
}

/**
 * Helper function to get the ordinal suffix for a given day.
 */
private fun getDayOfMonthSuffix(n: Int): String {
    return if (n in 11..13) {
        "th"
    } else {
        when (n % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }
}