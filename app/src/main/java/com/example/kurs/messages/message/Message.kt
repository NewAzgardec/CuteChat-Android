package com.example.kurs.messages.message

class Message (
    val id:String="",
    val senderId:String="",
    val senderName:String="",
    val receiverId:String="",
    val text:String="",
    val date: String="",
    val seen:Boolean = false
)