package com.lkuprashvili.chat.ui.chat

import com.lkuprashvili.chat.model.Conversation

sealed class ChatListState {
    data object Loading : ChatListState()
    data class Success(val chats: List<Conversation>) : ChatListState()
    data class Error(val message: String) : ChatListState()
    data object Empty : ChatListState()
}