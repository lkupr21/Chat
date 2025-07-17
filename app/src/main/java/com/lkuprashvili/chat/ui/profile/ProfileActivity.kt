package com.lkuprashvili.chat.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.lkuprashvili.chat.R
import com.lkuprashvili.chat.databinding.ActivityProfileBinding
import com.lkuprashvili.chat.utils.Const.IMAGE_URI
import com.lkuprashvili.chat.utils.Const.NICKNAME
import com.lkuprashvili.chat.utils.Const.PROFESSION
import com.lkuprashvili.chat.utils.Const.USER_PROFILE

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                selectedImageUri = data?.data
                selectedImageUri?.let {
                    Glide.with(this).load(it).into(binding.profileImage)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUserData()

        binding.profileImage.setOnClickListener { openGallery() }
        binding.changePhotoBtn.setOnClickListener { openGallery() }

        binding.saveBtn.setOnClickListener {
            saveUserData()
            finish()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun loadUserData() {
        val prefs = getSharedPreferences(USER_PROFILE, MODE_PRIVATE)
        val nickname = prefs.getString(NICKNAME, "")
        val profession = prefs.getString(PROFESSION, "")
        val imageUriString = prefs.getString(IMAGE_URI, null)

        binding.nicknameEt.setText(nickname)
        binding.professionEt.setText(profession)
        if (imageUriString != null) {
            Glide.with(this).load(Uri.parse(imageUriString)).into(binding.profileImage)
        } else {
            binding.profileImage.setImageResource(R.drawable.ic_profile)
        }
    }

    private fun saveUserData() {
        val prefs = getSharedPreferences(USER_PROFILE, MODE_PRIVATE).edit()
        prefs.putString(NICKNAME, binding.nicknameEt.text.toString())
        prefs.putString(PROFESSION, binding.professionEt.text.toString())
        selectedImageUri?.let {
            prefs.putString(IMAGE_URI, it.toString())
        }
        prefs.apply()
    }
}
