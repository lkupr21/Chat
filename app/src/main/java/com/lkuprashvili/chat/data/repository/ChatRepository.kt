package com.lkuprashvili.chat.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.lkuprashvili.chat.model.Conversation
import com.lkuprashvili.chat.utils.Const.CHATS
import com.lkuprashvili.chat.utils.Const.DB_ERROR
import com.lkuprashvili.chat.utils.Const.LAST_MESSAGE
import com.lkuprashvili.chat.utils.Const.NICKNAME
import com.lkuprashvili.chat.utils.Const.NOT_LOGGED_IN
import com.lkuprashvili.chat.utils.Const.PHOTO_URL
import com.lkuprashvili.chat.utils.Const.TIMESTAMP
import com.lkuprashvili.chat.utils.Const.USERS

class ChatRepository {
    private val db = FirebaseDatabase.getInstance().getReference(CHATS)
    private val usersDb = FirebaseDatabase.getInstance().getReference(USERS)
    private val auth = FirebaseAuth.getInstance()

    fun getUserConversations(
        onResult: (List<Conversation>) -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUid = auth.currentUser?.uid ?: return onError(NOT_LOGGED_IN)

        db.orderByChild(TIMESTAMP).get()
            .addOnSuccessListener { snapshot ->
                val result = mutableListOf<Conversation>()
                val chats = snapshot.children.filter { chatSnap ->
                    val users =
                        chatSnap.child(USERS).children.mapNotNull { it.getValue(String::class.java) }
                    users.contains(currentUid)
                }

                if (chats.isEmpty()) {
                    onResult(emptyList())
                    return@addOnSuccessListener
                }

                var processedCount = 0
                for (chatSnap in chats) {
                    val users =
                        chatSnap.child(USERS).children.mapNotNull { it.getValue(String::class.java) }
                    val otherUserId = users.firstOrNull { it != currentUid }

                    if (otherUserId == null) {
                        processedCount++
                        if (processedCount == chats.size) {
                            onResult(result.sortedByDescending { it.timestamp })
                        }
                        continue
                    }

                    usersDb.child(otherUserId).get().addOnSuccessListener { userSnap ->
                        val userName = userSnap.child(NICKNAME).value?.toString() ?: "Unknown"
                        val userPhoto = userSnap.child(PHOTO_URL).value?.toString() ?: ""

                        val convo = Conversation(
                            chatId = chatSnap.key ?: "",
                            lastMessage = chatSnap.child(LAST_MESSAGE).value?.toString() ?: "",
                            timestamp = chatSnap.child(TIMESTAMP).value as? Long ?: 0L,
                            userId = otherUserId,
                            userName = userName,
                            userPhoto = userPhoto
                        )
                        result.add(convo)

                        processedCount++
                        if (processedCount == chats.size) {
                            onResult(result.sortedByDescending { it.timestamp })
                        }
                    }.addOnFailureListener {
                        processedCount++
                        if (processedCount == chats.size) {
                            onResult(result.sortedByDescending { it.timestamp })
                        }
                    }
                }
            }
            .addOnFailureListener {
                onError(it.localizedMessage ?: DB_ERROR)
            }
    }
}