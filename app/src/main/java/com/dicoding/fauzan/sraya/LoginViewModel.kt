package com.dicoding.fauzan.sraya

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel(val loginRepository: LoginRepository) : ViewModel() {
    fun loginFirebase(firebaseAuth: FirebaseAuth, email: String, password: String) =
        loginRepository.loginFirebase(firebaseAuth, email, password)
}