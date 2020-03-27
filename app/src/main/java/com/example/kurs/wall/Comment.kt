package com.example.kurs.wall

import java.util.*

data class Comment (
    val text:String = "",
    val sender:String = "",
    val date: Date =Date(),
    val likes:Int =0,
    val isLiked:Boolean = false,
    val users:ArrayList<String>? = null
    )