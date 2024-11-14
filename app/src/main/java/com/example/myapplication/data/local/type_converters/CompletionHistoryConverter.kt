package com.example.myapplication.data.local.type_converters

import androidx.room.TypeConverter
import com.example.myapplication.model.CompletionEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CompletionHistoryConverter {
    private val gson = Gson()
    private val type = object : TypeToken<List<CompletionEntry>>() {}.type

    @TypeConverter
    fun fromCompletionHistory(history: List<CompletionEntry>?): String? {
        return history?.let { gson.toJson(it, type) }
    }

    @TypeConverter
    fun toCompletionHistory(json: String?): List<CompletionEntry>? {
        return json?.let { gson.fromJson(it, type) }
    }
}