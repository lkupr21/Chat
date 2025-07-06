package com.lkuprashvili.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lkuprashvili.chat.data.repository.AuthRepository

class AuthViewModel : ViewModel() {
    private val repo = AuthRepository()

    val authResult = MutableLiveData<Pair<Boolean, String?>>()

    fun login(email: String, password: String) {
        repo.login(email, password) { success, error ->
            authResult.value = Pair(success, error)
        }
    }

    fun register(email: String, password: String, nickname: String, profession: String) {
        repo.register(email, password, nickname, profession) { success, error ->
            authResult.value = Pair(success, error)
        }
    }
}