package com.lkuprashvili.chat.ui.chat

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lkuprashvili.chat.R
import com.lkuprashvili.chat.databinding.ActivityChatBinding
import com.lkuprashvili.chat.model.Message
import com.lkuprashvili.chat.ui.MessagesAdapter
import com.lkuprashvili.chat.utils.Const
import com.lkuprashvili.chat.utils.Const.CHAT_ID
import com.lkuprashvili.chat.utils.Const.CONVERSATIONS
import com.lkuprashvili.chat.utils.Const.LAST_MESSAGE
import com.lkuprashvili.chat.utils.Const.TIMESTAMP
import com.lkuprashvili.chat.utils.Const.USER_ID
import com.lkuprashvili.chat.utils.Const.USER_NAME
import com.lkuprashvili.chat.utils.dpToPx

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
        setupAppBarScrollListener()

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
        binding.profileLogo.setImageResource(R.drawable.avatar_image_placeholder)
        binding.backArrow.setOnClickListener {
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

    private fun setupAppBarScrollListener() {
        val collapsedPadding = 24.dpToPx(this)
        binding.messageInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.messagesRecyclerView.post {
                    binding.messagesRecyclerView.scrollToPosition(messagesAdapter.itemCount - 1)
                }
            }
        }


        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            val isCollapsed = totalScrollRange + verticalOffset <= 0

            if (isCollapsed) {
                binding.userNameRoleContainer.setPadding(collapsedPadding, collapsedPadding, collapsedPadding, collapsedPadding)

                binding.userNameRoleContainer.orientation = LinearLayout.HORIZONTAL

                val userNameParams = binding.userNameText.layoutParams as LinearLayout.LayoutParams
                userNameParams.topMargin = collapsedPadding / 2
                binding.userNameText.layoutParams = userNameParams

            } else {
                binding.userNameRoleContainer.setPadding(0, 0, 0, 0)
                binding.userNameRoleContainer.orientation = LinearLayout.VERTICAL
                binding.userRoleText.visibility = View.VISIBLE

                val userNameParams = binding.userNameText.layoutParams as LinearLayout.LayoutParams
                userNameParams.topMargin = 0
                binding.userNameText.layoutParams = userNameParams
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

        val otherUserId = intent.getStringExtra(Const.OTHER_USER_ID).orEmpty()
        val userName = intent.getStringExtra(Const.USER_NAME).orEmpty()

        val conversation = mapOf(
            CHAT_ID to chatId,
            USER_ID to otherUserId,
            USER_NAME to userName,
            LAST_MESSAGE to text,
            TIMESTAMP to System.currentTimeMillis()
        )

        val currentUserConversationRef =
            database.getReference(CONVERSATIONS).child(currentUserId).child(otherUserId)
        val otherUserConversationRef =
            database.getReference(CONVERSATIONS).child(otherUserId).child(currentUserId)

        currentUserConversationRef.setValue(conversation)
        otherUserConversationRef.setValue(conversation)
    }

}