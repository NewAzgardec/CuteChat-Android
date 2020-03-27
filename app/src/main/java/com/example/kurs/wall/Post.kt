package com.example.kurs.wall

import android.net.Uri
import java.util.*

data class Post (
    val id:String? = null,
    val date: Date =Date(),
    var sender:String = "",
    val isLiked:Boolean = false,
    val text:String = "",
    val uri: String? = null,
    val users:ArrayList<String>? = null,
    val likes:Int =0
    )