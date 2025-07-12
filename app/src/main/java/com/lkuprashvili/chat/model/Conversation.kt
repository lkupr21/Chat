package com.lkuprashvili.chat.model

data class Conversation(
    val chatId: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0L,
    val userId: String = "",
    val userName: String = "",
    val userPhoto: String = ""
)