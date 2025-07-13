package com.lkuprashvili.chat.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.lkuprashvili.chat.databinding.ActivityChatBinding
import com.lkuprashvili.chat.model.Message

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var messagesAdapter: MessagesAdapter

    private lateinit var chatId: String
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val database = FirebaseDatabase.getInstance()
    private lateinit var messagesRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatId = intent.getStringExtra("chatId") ?: ""

        if (chatId.isEmpty()) {
            Toast.makeText(this, "Invalid chat ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

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
        binding.sendButton.setOnClickListener {
            val messageText = binding.messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                binding.messageInput.setText("")
            }
        }
    }

    private fun listenForMessages() {
        messagesRef = database.getReference("messages").child(chatId)

        messagesRef.orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = mutableListOf<Message>()
                    for (child in snapshot.children) {
                        val message = child.getValue(Message::class.java)
                        if (message != null) {
                            messages.add(message)
                        }
                    }
                    messagesAdapter.submitList(messages)
                    binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChatActivity, "Failed to load messages", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun sendMessage(text: String) {
        val newMessageRef = messagesRef.push()
        val message = Message(
            messageId = newMessageRef.key ?: "",
            senderId = currentUserId,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        newMessageRef.setValue(message).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
            }
        }

        val chatRef = database.getReference("chats").child(chatId)
        chatRef.child("lastMessage").setValue(text)
        chatRef.child("timestamp").setValue(System.currentTimeMillis())
    }
}