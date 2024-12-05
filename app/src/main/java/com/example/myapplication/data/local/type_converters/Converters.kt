package com.example.myapplication.data.local.type_converters

import androidx.room.TypeConverter
import com.example.myapplication.data.AttachmentInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.datetime.LocalDate
import java.util.UUID

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let { LocalDate.fromEpochDays(it.toInt()) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDays()?.toLong()
    }

    @TypeConverter
    fun fromUUID(uuid: String?): UUID? {
        return uuid?.let { UUID.fromString(it) }
    }

    @TypeConverter
    fun uuidToString(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun fromAttachmentInfoList(attachments: List<AttachmentInfo>?): String? {
        return Gson().toJson(attachments)
    }

    @TypeConverter
    fun toAttachmentInfoList(json: String?): List<AttachmentInfo>? {
        return if (json == null) {
            emptyList()
        } else {
            val type = object : TypeToken<List<AttachmentInfo>>() {}.type
            Gson().fromJson(json, type)
        }
    }
}