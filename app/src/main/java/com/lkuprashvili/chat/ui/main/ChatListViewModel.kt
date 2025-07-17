package com.lkuprashvili.chat.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lkuprashvili.chat.model.Conversation
import com.lkuprashvili.chat.ui.chat.ChatListState
import com.lkuprashvili.chat.utils.Const.CONVERSATIONS
import com.lkuprashvili.chat.utils.Const.TIMESTAMP

class ChatListViewModel : ViewModel() {

    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    private val currentUid = auth.currentUser?.uid ?: ""

    private val _state = MutableLiveData<ChatListState>()
    val state: LiveData<ChatListState> get() = _state

    init {
        loadChats()
    }

    fun loadChats() {
        if (currentUid.isEmpty()) {
            _state.value = ChatListState.Error("მომხმარებელი ვერ მოიძებნა")
            return
        }

        _state.value = ChatListState.Loading

        db.child(CONVERSATIONS).child(currentUid)
            .orderByChild(TIMESTAMP)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        _state.value = ChatListState.Empty
                        return
                    }

                    val chatList = snapshot.children.mapNotNull {
                        it.getValue(Conversation::class.java)
                    }.sortedByDescending { it.timestamp }

                    if (chatList.isEmpty()) {
                        _state.value = ChatListState.Empty
                    } else {
                        _state.value = ChatListState.Success(chatList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _state.value = ChatListState.Error("შეცდომა: ${error.message}")
                }
            })
    }
}
