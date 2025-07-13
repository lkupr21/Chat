package com.lkuprashvili.chat.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.FirebaseAuth
import com.lkuprashvili.chat.R
import com.lkuprashvili.chat.databinding.ActivityMainBinding
import com.lkuprashvili.chat.ui.CustomBottomNavigationView
import com.lkuprashvili.chat.ui.auth.LoginActivity
import com.lkuprashvili.chat.ui.chat.ChatsFragment
import com.lkuprashvili.chat.ui.chat.NewChatActivity
import com.lkuprashvili.chat.ui.profile.ProfileFragment
import com.lkuprashvili.chat.ui.search.SearchFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<ChatListViewModel>()
    private lateinit var adapter: ChatAdapter
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.loadChats()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.searchView.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, SearchFragment())
                .addToBackStack(null)
                .commit()
        }

        val customBottomNav = findViewById<CustomBottomNavigationView>(R.id.customBottomNavigation)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, ChatsFragment())
            .commit()

        customBottomNav.findViewById<View>(R.id.navHome).setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ChatsFragment())
                .commit()
        }

        customBottomNav.findViewById<View>(R.id.navProfile).setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ProfileFragment())
                .commit()
        }
//        customBottomNav.findViewById<View>(R.id.fabCenter).setOnClickListener{
//                        startActivity(Intent(this, NewChatActivity::class.java))
//
//        }

        binding.fabCenter.setOnClickListener {
            startActivity(Intent(this, NewChatActivity::class.java))

        }
        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
    }

    fun setAppBarHeight(heightPx: Int) {
        val appBar = findViewById<AppBarLayout>(R.id.appBarLayout)
        appBar?.layoutParams = appBar?.layoutParams?.apply {
            height = heightPx
        }
        appBar?.requestLayout()
    }
}
