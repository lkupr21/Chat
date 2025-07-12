package com.lkuprashvili.chat.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.lkuprashvili.chat.R
import com.lkuprashvili.chat.databinding.ActivityMainBinding
import com.lkuprashvili.chat.ui.ChatActivity
import com.lkuprashvili.chat.ui.NewChatActivity

import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<ChatListViewModel>()
    private lateinit var adapter: ChatAdapter
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ChatAdapter { chat ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chatId", chat.chatId)
            intent.putExtra("otherUserId", chat.userId)
            intent.putExtra("userName", chat.userName)
            startActivity(intent)
        }

        binding.recyclerView.adapter = adapter

        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            viewModel.loadChats()
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }

        viewModel.conversations.observe(this) {
            adapter.submitList(it)
        }

        viewModel.error.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        binding.fabNewChat.setOnClickListener {
            startActivity(Intent(this, NewChatActivity::class.java))
        }
    }
}