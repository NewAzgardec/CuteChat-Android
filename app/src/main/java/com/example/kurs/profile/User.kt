package com.example.kurs.profile

data class User (
    val id:String = "",
    val username: String = "",
    val lowerName: String = "",
    val email:String= "",
    val password:String = "",
    val imageUri:String? = "",
    val newFriends:HashMap<String, Any>? = null,
    val friends:HashMap<String, Any>? = null,
    var onlineStatus:String = ""
)