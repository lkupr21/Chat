package com.lkuprashvili.chat.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.lkuprashvili.chat.model.User
import com.lkuprashvili.chat.utils.Const.AUTH_FAILED
import com.lkuprashvili.chat.utils.Const.DB_ERROR
import com.lkuprashvili.chat.utils.Const.NICKNAME
import com.lkuprashvili.chat.utils.Const.NICKNAME_CHECK_FAILED

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().getReference("users")
    fun register(
        email: String,
        password: String,
        nickname: String,
        profession: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        db.orderByChild(NICKNAME).equalTo(nickname).get()
            .addOnSuccessListener {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                            val user = User(uid, email, nickname, profession)

                            db.child(uid).setValue(user)
                                .addOnSuccessListener {
                                    onResult(true, null)
                                }
                                .addOnFailureListener { e ->
                                    onResult(false, e.message ?: DB_ERROR)
                                }
                        } else {
                            onResult(false, task.exception?.localizedMessage ?: AUTH_FAILED)
                        }
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message ?: NICKNAME_CHECK_FAILED)
            }
    }


    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) onResult(true, null)
            else onResult(false, it.exception?.message)
        }
    }
}