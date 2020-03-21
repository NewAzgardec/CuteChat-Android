package com.example.kurs.profile

import com.google.firebase.database.PropertyName

data class User (
    val id:String = "",
    val username: String = "",
    val lowerName: String = "",
    val email:String= "",
    val password:String = "",
    val friends:HashMap<String, Any>? = null,
    val onlineStatus:String = ""
)