package com.example.kurs.common

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun stringToHash(json: String): HashMap<String, String> {
        val gson = Gson()
        val type = object : TypeToken<HashMap<String, String>>() {
        }.type
        return gson.fromJson<HashMap<String, String>>(json, type)
    }

    @TypeConverter
    fun hashToString(list: HashMap<String, String>): String {
        val gson = Gson()
        val type = object : TypeToken<HashMap<String, String>>() {
        }.type
        return gson.toJson(list, type)
    }
}