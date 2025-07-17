package com.lkuprashvili.chat.ui.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lkuprashvili.chat.databinding.ActivityNewChatBinding
import com.lkuprashvili.chat.model.Conversation
import com.lkuprashvili.chat.model.User
import com.lkuprashvili.chat.ui.UserAdapter
import com.lkuprashvili.chat.utils.Const
import com.lkuprashvili.chat.utils.Const.CONVERSATIONS
import com.lkuprashvili.chat.utils.Const.MESSAGES
import com.lkuprashvili.chat.utils.Const.TIMESTAMP
import com.lkuprashvili.chat.utils.generateChatId

class NewChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewChatBinding
    private lateinit var adapter: UserAdapter
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    private val currentUid by lazy { auth.currentUser?.uid ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.usersRv.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter { user -> openChatWithUser(user) }
        binding.usersRv.adapter = adapter

        loadUsersWithLastMessages()
    }

    private fun openChatWithUser(user: User) {
        val chatId = generateChatId(currentUid, user.userId)

        val conversation = Conversation(
            chatId = chatId,
            userId = user.userId,
            userName = user.nickname,
            userRole = user.profession,
            lastMessage = user.lastMessage,
            timestamp = System.currentTimeMillis()
        )

        db.child(CONVERSATIONS).child(currentUid).child(user.userId).setValue(conversation)
        db.child(CONVERSATIONS).child(user.userId).child(currentUid).setValue(conversation)

        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra(Const.CHAT_ID, chatId)
            putExtra(Const.OTHER_USER_ID, user.userId)
            putExtra(Const.USER_NAME, user.nickname)
            putExtra(Const.USER_ROLE, user.profession)
        }
        startActivity(intent)
        finish()
    }

    private fun loadUsersWithLastMessages() {
        if (currentUid.isEmpty()) return

        db.child(Const.USERS).get().addOnSuccessListener { snapshot ->
            val users = snapshot.children.mapNotNull {
                val user = it.getValue(User::class.java)
                user?.copy(userId = it.key ?: "")
            }.filter { it.userId != currentUid }.toMutableList()

            updateUsersWithLastMessages(users)
        }
    }

    private fun updateUsersWithLastMessages(users: MutableList<User>) {
        val updatedUsers = mutableListOf<User>()
        var processedCount = 0

        if (users.isEmpty()) {
            adapter.submitList(users)
            return
        }

        users.forEach { user ->
            val chatId = generateChatId(currentUid, user.userId)

            db.child(Const.CHATS).child(chatId).child(MESSAGES).orderByChild(TIMESTAMP)
                .limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val lastMessage = if (snapshot.exists()) {
                            snapshot.children.first().child("text").getValue(String::class.java)
                                .orEmpty()
                        } else {
                            ""
                        }

                        val updatedUser = user.copy(lastMessage = lastMessage)
                        updatedUsers.add(updatedUser)
                        processedCount++

                        if (processedCount == users.size) {
                            updatedUsers.sortByDescending { it.lastMessage.isNotEmpty() }
                            adapter.submitList(updatedUsers)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        processedCount++
                        if (processedCount == users.size) {
                            adapter.submitList(updatedUsers)
                        }
                    }
                })
        }
    }
}