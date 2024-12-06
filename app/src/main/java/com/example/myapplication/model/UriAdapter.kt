package com.example.myapplication.model

import android.net.Uri
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

class UriAdapter : TypeAdapter<Uri>() {
    override fun write(out: JsonWriter, value: Uri?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.toString())  // ISO-8601 format (yyyy-MM-dd)
        }
    }

    override fun read(input: JsonReader): Uri? {
        val uriStr = input.nextString()
        return if (uriStr.isNullOrEmpty()) null else Uri.parse(uriStr)
    }
}