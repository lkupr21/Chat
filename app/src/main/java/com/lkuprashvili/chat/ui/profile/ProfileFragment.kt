package com.lkuprashvili.chat.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.lkuprashvili.chat.R
import com.lkuprashvili.chat.databinding.FragmentProfileBinding
import com.lkuprashvili.chat.ui.auth.LoginActivity
import com.lkuprashvili.chat.utils.Const.FAILED_TO_LOAD_PROFILE
import com.lkuprashvili.chat.utils.Const.NICKNAME
import com.lkuprashvili.chat.utils.Const.PHOTO_UPLOADED_SUCCESSFULLY
import com.lkuprashvili.chat.utils.Const.PHOTO_URL
import com.lkuprashvili.chat.utils.Const.PROFESSION
import com.lkuprashvili.chat.utils.Const.PROFILE_UPLOADED
import com.lkuprashvili.chat.utils.Const.UPDATE_FAILED
import com.lkuprashvili.chat.utils.Const.USERS

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference.child(USERS)

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let {
                Glide.with(this)
                    .load(it)
                    .circleCrop() // ✅ დაამატე ეს
                    .into(binding.profilePhoto)

                // შეინახე ლოკალურად
                saveImageUriLocally(it)
            }
        }
    }
    private fun saveImageUriLocally(uri: Uri) {
        val prefs = requireContext().getSharedPreferences("user_profile", AppCompatActivity.MODE_PRIVATE)
        prefs.edit().putString("profile_image_uri", uri.toString()).apply()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
            return
        }

        loadUserProfile()

        binding.profilePhoto.setOnClickListener {
            openGalleryForImage()
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        binding.btnSave.setOnClickListener {
            saveProfileChanges()
        }
    }


    private fun loadUserProfile() {
        val currentUserId = auth.currentUser?.uid ?: return

        // წამოიღე Firebase Database-დან
        database.child(currentUserId).get()
            .addOnSuccessListener { dataSnapshot ->
                val nickname = dataSnapshot.child(NICKNAME).getValue(String::class.java) ?: ""
                val profession = dataSnapshot.child(PROFESSION).getValue(String::class.java) ?: ""
                val photoUrl = dataSnapshot.child(PHOTO_URL).getValue(String::class.java)

                binding.etNickname.setText(nickname)
                binding.etProfession.setText(profession)

                if (!photoUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(photoUrl)
                        .circleCrop()
                        .into(binding.profilePhoto)
                } else {
                    binding.profilePhoto.setImageResource(R.drawable.ic_profile)
                }

                val prefs = requireContext().getSharedPreferences("user_profile", AppCompatActivity.MODE_PRIVATE).edit()
                prefs.putString(NICKNAME, nickname)
                prefs.putString(PROFESSION, profession)
                photoUrl?.let { prefs.putString("profile_image_uri", it) }
                prefs.apply()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), FAILED_TO_LOAD_PROFILE, Toast.LENGTH_SHORT).show()
            }
    }



    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }


    private fun uploadProfilePhoto(uri: Uri) {
        val currentUserId = auth.currentUser?.uid ?: return

        val storageRef = FirebaseStorage.getInstance()
            .reference
            .child("profile_photos/$currentUserId.jpg")

        storageRef.putFile(uri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                storageRef.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                database.child(currentUserId).child(PHOTO_URL)
                    .setValue(downloadUri.toString())

                Glide.with(this)
                    .load(downloadUri)
                    .circleCrop()
                    .into(binding.profilePhoto)

                Toast.makeText(requireContext(), PHOTO_UPLOADED_SUCCESSFULLY, Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Upload failed: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
    }


    override fun onResume() {
        super.onResume()
        val appBar = (activity as? AppCompatActivity)?.findViewById<AppBarLayout>(R.id.appBarLayout)
        appBar?.layoutParams?.height = 0
        appBar?.requestLayout()
    }

    override fun onPause() {
        super.onPause()
        requireActivity().findViewById<View>(R.id.appBarLayout)?.visibility = View.VISIBLE
    }


    private fun saveProfileChanges() {
        val prefs = requireContext().getSharedPreferences("user_profile", AppCompatActivity.MODE_PRIVATE)
        prefs.edit()
            .putString(NICKNAME, binding.etNickname.text.toString().trim())
            .putString(PROFESSION, binding.etProfession.text.toString().trim())
            .apply()

        Toast.makeText(requireContext(), "პროფილი შეინახა", Toast.LENGTH_SHORT).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
