package com.lkuprashvili.chat.model

data class User(
    val userId: String = "",
    val email: String = "",
    val nickname: String = "",
    val profession: String = "",
    val photoUrl: String = "",
    val lastMessage: String = ""
)