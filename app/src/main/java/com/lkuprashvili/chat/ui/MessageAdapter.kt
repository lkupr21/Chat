package com.lkuprashvili.chat.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lkuprashvili.chat.databinding.ItemMessageIncomingBinding
import com.lkuprashvili.chat.databinding.ItemMessageOutgoingBinding
import com.lkuprashvili.chat.model.Message

class MessagesAdapter(private val currentUserId: String) :
    ListAdapter<Message, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    private val TYPE_OUTGOING = 1
    private val TYPE_INCOMING = 2

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).senderId == currentUserId) TYPE_OUTGOING else TYPE_INCOMING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_OUTGOING) {
            val binding = ItemMessageOutgoingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            OutgoingViewHolder(binding)
        } else {
            val binding = ItemMessageIncomingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            IncomingViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        if (holder is OutgoingViewHolder) {
            holder.bind(message)
        } else if (holder is IncomingViewHolder) {
            holder.bind(message)
        }
    }

    inner class OutgoingViewHolder(private val binding: ItemMessageOutgoingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.messageText.text = message.text
        }
    }

    inner class IncomingViewHolder(private val binding: ItemMessageIncomingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.messageText.text = message.text
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(oldItem: Message, newItem: Message) =
                oldItem.messageId == newItem.messageId

            override fun areContentsTheSame(oldItem: Message, newItem: Message) =
                oldItem == newItem
        }
    }
}
