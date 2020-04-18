package com.example.kurs.wall

import java.util.*

data class Post(
    val id: String? = null,
    val date: Date = Date(),
    var sender: String = "",
    var isLiked: Boolean = false,
    val text: String = "",
    val uri: String? = null,
    val users: HashMap<String, Any>? = null,
    var senderName: String? = ""
)