package com.lkuprashvili.chat.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lkuprashvili.chat.data.repository.ChatRepository
import com.lkuprashvili.chat.model.Conversation
import com.lkuprashvili.chat.ui.chat.ChatListState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatListViewModel : ViewModel() {

    private val _state = MutableLiveData<ChatListState>()
    val state: LiveData<ChatListState> get() = _state

    private var fullChatList: List<Conversation> = emptyList()
    private var searchJob: Job? = null

    fun loadChats() {
        _state.value = ChatListState.Loading

        ChatRepository().getUserConversations(
            onResult = { list ->
                fullChatList = list
                if (list.isEmpty()) {
                    _state.value = ChatListState.Empty
                } else {
                    _state.value = ChatListState.Success(list)
                }
            },
            onError = { err ->
                _state.value = ChatListState.Error(err)
            }
        )
    }

    fun filterChats(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)

            if (query.length < 3) {
                _state.value = ChatListState.Success(fullChatList)
                return@launch
            }

            val filtered = fullChatList.filter {
                it.userName.contains(query, ignoreCase = true)
            }

            if (filtered.isEmpty()) {
                _state.value = ChatListState.Empty
            } else {
                _state.value = ChatListState.Success(filtered)
            }
        }
    }
}