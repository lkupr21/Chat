package com.lkuprashvili.chat.ui.chat

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.lkuprashvili.chat.databinding.ActivityNewChatBinding
import com.lkuprashvili.chat.model.User
import com.lkuprashvili.chat.ui.UserAdapter
import com.lkuprashvili.chat.utils.Const
import com.lkuprashvili.chat.utils.generateChatId

class NewChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewChatBinding
    private lateinit var adapter: UserAdapter
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.usersRv.layoutManager = LinearLayoutManager(this)

        adapter = UserAdapter { user ->
            val currentUserId = auth.currentUser?.uid ?: return@UserAdapter
            val chatId = generateChatId(currentUserId, user.userId)

            val chatMap = mapOf(
                Const.USERS to mapOf(
                    Const.ZERO to currentUserId,
                    Const.ONE to user.userId
                ),
                Const.LAST_MESSAGE to "სალამი",
                Const.TIMESTAMP to System.currentTimeMillis()
            )

            db.child(Const.CHATS).child(chatId).setValue(chatMap).addOnSuccessListener {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra(Const.CHAT_ID, chatId)
                intent.putExtra(Const.OTHER_USER_ID, user.userId)
                intent.putExtra(Const.USER_NAME, user.nickname)
                startActivity(intent)
                finish()
            }
        }

        binding.usersRv.adapter = adapter

        loadUsers()
    }

    private fun loadUsers() {
        val currentUid = auth.currentUser?.uid ?: return

        db.child(Const.USERS).get().addOnSuccessListener { snapshot ->
            val userList = snapshot.children.mapNotNull {
                val user = it.getValue(User::class.java)
                user?.copy(userId = it.key ?: "")
            }.filter { it.userId != currentUid }

            adapter.submitList(userList)
        }
    }

}