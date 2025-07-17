package com.lkuprashvili.chat.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.lkuprashvili.chat.databinding.ActivityRegisterBinding
import com.lkuprashvili.chat.ui.main.MainActivity
import com.lkuprashvili.chat.utils.Const.FILL_ALL_FIELDS
import com.lkuprashvili.chat.utils.Const.REGISTRATION_FAILED
import com.lkuprashvili.chat.viewmodel.AuthViewModel

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel by viewModels<AuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerBtn.setOnClickListener {
            val nickname = binding.nicknameEt.text.toString().trim()
            val password = binding.passwordEt.text.toString().trim()
            val profession = binding.professionEt.text.toString().trim()

            if (nickname.isEmpty() || password.isEmpty() || profession.isEmpty()) {
                Toast.makeText(this, FILL_ALL_FIELDS, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fakeEmail = "$nickname@myapp.com"
            viewModel.register(fakeEmail, password, nickname, profession)
        }

        viewModel.authResult.observe(this) { (success, error) ->
            if (success) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, error ?: REGISTRATION_FAILED, Toast.LENGTH_SHORT).show()
            }
        }
    }
}