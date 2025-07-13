package com.lkuprashvili.chat.ui.chat

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lkuprashvili.chat.databinding.ActivityChatBinding
import com.lkuprashvili.chat.model.Message
import com.lkuprashvili.chat.ui.MessagesAdapter
import com.lkuprashvili.chat.utils.Const

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var messagesAdapter: MessagesAdapter

    private lateinit var chatId: String
    private lateinit var userName: String

    private lateinit var userRole: String

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val database = FirebaseDatabase.getInstance()
    private lateinit var messagesRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatId = intent.getStringExtra(Const.CHAT_ID).orEmpty()
        userName = intent.getStringExtra(Const.USER_NAME).orEmpty()
        userRole = intent.getStringExtra(Const.USER_ROLE).orEmpty()

        if (chatId.isEmpty()) {
            Toast.makeText(this, Const.INVALID_CHAT_ID, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.userNameText.text = userName
        binding.userRoleText.text = userRole

        setupRecyclerView()
        setupSendButton()
        listenForMessages()
    }

    private fun setupRecyclerView() {
        messagesAdapter = MessagesAdapter(currentUserId)
        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = messagesAdapter
        }
    }

    private fun setupSendButton() {
//
//        binding.userNameText.text = userName
//        binding.userRoleText.text = userRole

//        Glide.with(this)
//            .load(userPhoto)
//            .placeholder(R.drawable.ic_person)
//            .into(binding.userProfileImage)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.sendButton.setOnClickListener {
            val messageText = binding.messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                binding.messageInput.setText("")
            }
        }
    }

    private fun listenForMessages() {
        messagesRef = database.getReference(Const.MESSAGES).child(chatId)

        messagesRef.orderByChild(Const.TIMESTAMP)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = mutableListOf<Message>()
                    for (child in snapshot.children) {
                        val message = child.getValue(Message::class.java)
                        if (message != null) {
                            messages.add(message)
                        }
                    }
                    messagesAdapter.submitList(messages) {
                        binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChatActivity, Const.FAILED_TO_LOAD, Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun sendMessage(text: String) {
        val newMessageRef = messagesRef.push()
        val message = Message(
            messageId = newMessageRef.key.orEmpty(),
            senderId = currentUserId,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        newMessageRef.setValue(message).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Toast.makeText(this, Const.FAILED_TO_SEND, Toast.LENGTH_SHORT).show()
            }
        }

        val chatRef = database.getReference(Const.CHATS).child(chatId)
        chatRef.child(Const.LAST_MESSAGE).setValue(text)
        chatRef.child(Const.TIMESTAMP).setValue(System.currentTimeMillis())
    }
}