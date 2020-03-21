package com.example.kurs.current

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class CurrentUser (
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id:String = "",

    @ColumnInfo(name = "username")
    val username: String = "",

    @ColumnInfo(name = "email")
    val email:String= "",

    @ColumnInfo(name = "password")
    val password:String = "",

    @ColumnInfo(name = "friends")
    val friends:HashMap<String, Any>? = null
)