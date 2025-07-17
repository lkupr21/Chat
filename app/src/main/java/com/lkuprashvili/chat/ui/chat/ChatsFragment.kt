package com.lkuprashvili.chat.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.lkuprashvili.chat.R
import com.lkuprashvili.chat.databinding.FragmentChatsBinding
import com.lkuprashvili.chat.model.Conversation
import com.lkuprashvili.chat.ui.main.ChatAdapter
import com.lkuprashvili.chat.ui.main.ChatListViewModel
import com.lkuprashvili.chat.utils.AppBarManager
import com.lkuprashvili.chat.utils.Const

class ChatsFragment : Fragment() {
    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<ChatListViewModel>()

    private lateinit var adapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ChatAdapter { chat ->
            val intent = Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra(Const.CHAT_ID, chat.chatId)
            intent.putExtra(Const.OTHER_USER_ID, chat.userId)
            intent.putExtra(Const.USER_NAME, chat.userName)
            intent.putExtra(Const.USER_ROLE,chat.userRole)
            startActivity(intent)
        }
        binding.recyclerView.adapter = adapter

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ChatListState.Loading -> showLoadingState()
                is ChatListState.Empty -> showEmptyState()
                is ChatListState.Error -> showErrorState(state.message)
                is ChatListState.Success -> showContent(state.chats)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        val appBar = (activity as? AppCompatActivity)?.findViewById<AppBarLayout>(R.id.appBarLayout)
        AppBarManager.show(appBar)
    }


    private fun showLoadingState() {
        binding.recyclerView.visibility = View.GONE
        binding.stateView.visibility = View.VISIBLE
        binding.stateIcon.setImageResource(R.drawable.loading)
        binding.stateText.text = Const.LOADING
    }

    private fun showEmptyState() {
        binding.recyclerView.visibility = View.GONE
        binding.stateView.visibility = View.VISIBLE
        binding.stateIcon.setImageResource(R.drawable.out_of_stock)
        binding.stateText.text = Const.NO_CHATS
    }

    private fun showErrorState(message: String) {
        binding.recyclerView.visibility = View.GONE
        binding.stateView.visibility = View.VISIBLE
        binding.stateIcon.setImageResource(R.drawable.cross)
        binding.stateText.text = message
    }

    private fun showContent(chats: List<Conversation>) {
        binding.stateView.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        adapter.submitList(chats)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}