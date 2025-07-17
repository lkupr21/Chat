package com.lkuprashvili.chat.ui.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.lkuprashvili.chat.R
import com.lkuprashvili.chat.databinding.FragmentSearchBinding
import com.lkuprashvili.chat.model.User
import com.lkuprashvili.chat.ui.chat.ChatActivity
import com.lkuprashvili.chat.ui.UserAdapter
import com.lkuprashvili.chat.ui.main.MainActivity
import com.lkuprashvili.chat.utils.Const
import com.lkuprashvili.chat.utils.generateChatId

class SearchFragment : Fragment(R.layout.fragment_search) {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseDatabase.getInstance().reference

    private lateinit var usersAdapter: UserAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)

        usersAdapter = UserAdapter { user ->
            openChat(user)
        }

        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.usersRecyclerView.apply {
            adapter = usersAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        loadUsers()
        setupSearch()
    }


    private fun loadUsers() {
        val currentUserId = ""

        db.child("users").get().addOnSuccessListener { snapshot ->
            val users = snapshot.children.mapNotNull {
                val user = it.getValue(User::class.java)
                user?.copy(userId = it.key ?: "")
            }.filter { it.userId != currentUserId }

            updateUi(users)
        }.addOnFailureListener {
            updateUi(emptyList())
        }
    }

    private fun openChat(user: User) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val chatId = generateChatId(currentUserId, user.userId)

        val intent = Intent(requireContext(), ChatActivity::class.java)
        intent.putExtra(Const.CHAT_ID, chatId)
        intent.putExtra(Const.OTHER_USER_ID, user.userId)
        intent.putExtra(Const.USER_NAME, user.nickname)
        intent.putExtra(Const.USER_ROLE, user.profession)
        startActivity(intent)
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank() || newText.length < 3) {
                    loadUsers()
                    return true
                }

                val filteredList = usersAdapter.currentList.filter { user ->
                    user.nickname.contains(newText, ignoreCase = true)
                }
                updateUi(filteredList)
                return true
            }
        })
    }

    private fun updateUi(users: List<User>) {
        if (_binding == null) return

        if (users.isEmpty()) {
            binding.noResultsTextView.visibility = View.VISIBLE
            binding.usersRecyclerView.visibility = View.GONE
        } else {
            binding.noResultsTextView.visibility = View.GONE
            binding.usersRecyclerView.visibility = View.VISIBLE
            usersAdapter.submitList(users)
        }
    }


    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setAppBarHeight(0)
    }


    override fun onPause() {
        super.onPause()
        (activity as? MainActivity)?.setAppBarHeight(0)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}