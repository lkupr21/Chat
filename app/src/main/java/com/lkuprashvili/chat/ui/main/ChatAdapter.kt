package com.lkuprashvili.chat.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lkuprashvili.chat.R
import com.lkuprashvili.chat.databinding.ItemConversationBinding
import com.lkuprashvili.chat.model.Conversation
import com.lkuprashvili.chat.utils.toTimeFormat

class ChatAdapter(val onClick: (Conversation) -> Unit) :
    ListAdapter<Conversation, ChatAdapter.ChatViewHolder>(DIFF) {

    inner class ChatViewHolder(val binding: ItemConversationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Conversation) = with(binding) {
            nameTv.text = item.userName
            messageTv.text = item.lastMessage
            timeTv.text = item.timestamp.toTimeFormat()

            val defaultAvatars = listOf(
                R.drawable.ic_profile,
                R.drawable.ic_man,
                R.drawable.man
            )

            val photoUrl = item.userPhoto
            if (photoUrl.isEmpty()) {
                val randomAvatar = defaultAvatars.random()
                Glide.with(profileIv.context)
                    .load(randomAvatar)
                    .into(profileIv)
            } else {
                Glide.with(profileIv.context)
                    .load(photoUrl)
                    .placeholder(R.drawable.boy)
                    .into(profileIv)
            }

            root.setOnClickListener { onClick(item) }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ChatViewHolder(
        ItemConversationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Conversation>() {
            override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation) =
                oldItem.chatId == newItem.chatId

            override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation) =
                oldItem == newItem
        }
    }
}
