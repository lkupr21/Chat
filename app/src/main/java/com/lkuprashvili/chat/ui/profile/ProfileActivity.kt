package com.lkuprashvili.chat.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.lkuprashvili.chat.R
import com.lkuprashvili.chat.utils.Const.IMAGE_URI
import com.lkuprashvili.chat.utils.Const.NICKNAME
import com.lkuprashvili.chat.utils.Const.PROFESSION
import com.lkuprashvili.chat.utils.Const.USER_PROFILE

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var changePhotoBtn: Button
    private lateinit var nicknameEt: EditText
    private lateinit var professionEt: EditText
    private lateinit var saveBtn: Button

    private var selectedImageUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                selectedImageUri = data?.data
                selectedImageUri?.let {
                    Glide.with(this).load(it).into(profileImage)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)


        loadUserData()

        profileImage.setOnClickListener { openGallery() }
        changePhotoBtn.setOnClickListener { openGallery() }

        saveBtn.setOnClickListener {
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

        nicknameEt.setText(nickname)
        professionEt.setText(profession)
        if (imageUriString != null) {
            Glide.with(this).load(Uri.parse(imageUriString)).into(profileImage)
        } else {
            profileImage.setImageResource(R.drawable.ic_profile)
        }
    }

    private fun saveUserData() {
        val prefs = getSharedPreferences(USER_PROFILE, MODE_PRIVATE).edit()
        prefs.putString(NICKNAME, nicknameEt.text.toString())
        prefs.putString(PROFESSION, professionEt.text.toString())
        selectedImageUri?.let {
            prefs.putString(IMAGE_URI, it.toString())
        }
        prefs.apply()
    }
}