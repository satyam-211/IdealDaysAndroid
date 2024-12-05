package com.example.myapplication.model

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlinx.datetime.LocalDate

class LocalDateAdapter : TypeAdapter<LocalDate>() {
    override fun write(out: JsonWriter, value: LocalDate?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.toString())  // ISO-8601 format (yyyy-MM-dd)
        }
    }

    override fun read(input: JsonReader): LocalDate? {
        val dateStr = input.nextString()
        return if (dateStr.isNullOrEmpty()) null else LocalDate.parse(dateStr)
    }
}