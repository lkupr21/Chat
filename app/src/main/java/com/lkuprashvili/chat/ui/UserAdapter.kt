package com.lkuprashvili.chat.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lkuprashvili.chat.R
import com.lkuprashvili.chat.databinding.ItemUserBinding
import com.lkuprashvili.chat.model.User

class UserAdapter(private val onClick: (User) -> Unit) :
    ListAdapter<User, UserAdapter.UserViewHolder>(DIFF) {

    private val defaultAvatars = listOf(
        R.drawable.ic_profile,
        R.drawable.ic_man,
        R.drawable.man
    )

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.nameTv.text = user.nickname

            val photoUrl = user.photoUrl
            if (photoUrl.isEmpty()) {
                val randomAvatar = defaultAvatars.random()
                Glide.with(binding.profileIv.context)
                    .load(randomAvatar)
                    .into(binding.profileIv)
            } else {
                Glide.with(binding.profileIv.context)
                    .load(photoUrl)
                    .into(binding.profileIv)
            }

            binding.root.setOnClickListener { onClick(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User) =
                oldItem.userId == newItem.userId

            override fun areContentsTheSame(oldItem: User, newItem: User) =
                oldItem == newItem
        }
    }
}
