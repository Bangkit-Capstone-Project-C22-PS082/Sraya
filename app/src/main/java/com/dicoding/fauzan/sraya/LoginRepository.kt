package com.dicoding.fauzan.sraya

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.liveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginRepository(
    val sessionPreferences: SessionPreferences,
    val context: Context) {
    val database = Firebase.firestore

    fun loginFirebase(firebaseAuth: FirebaseAuth, email: String, password: String) = liveData {
        emit(Result.Loading)

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener() {
                if (it.isSuccessful){
                    database.collection("Users").whereEqualTo("Email", email).get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                Log.d(TAG, "${document.get("NIK")}, ${document.get("Nama")}")
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, exception.message.toString())
                        }
                    Result.Success(true)
                    Log.d(TAG, "A successful sign-in")
                }
                else{
                    Result.Error("Sign in error")
                }
            }

    }
    companion object {
        private val TAG = this::class.java.simpleName
        @Volatile
        private var INSTANCE: LoginRepository? = null

        fun getInstance(
            sessionPreferences: SessionPreferences,
            context: Context
        ): LoginRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LoginRepository(sessionPreferences, context).also {
                    INSTANCE = it
                }
            }
        }
    }
}