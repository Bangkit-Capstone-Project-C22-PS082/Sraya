package com.dicoding.fauzan.sraya

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.dicoding.fauzan.sraya.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.signin.SignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("session")
    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var viewModel: LoginViewModel
    private lateinit var database: FirebaseFirestore
    private lateinit var sessionPreferences: SessionPreferences

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)!!

                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this) {
                    if (it.isSuccessful) {
                        // Sign in success
                        val user = firebaseAuth.currentUser
                        updateUI(user)
                    } else {
                        // Sign in failed
                        updateUI(null)
                    }
                }
            } catch (e: ApiException) {

            }
        }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        currentUser?.let {
            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        /*
        viewModel = ViewModelProvider(
            this, LoginViewModelFactory.getInstance(dataStore))[LoginViewModel::class.java]

         */
        database = Firebase.firestore
        setupAction()

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnLoginGoogle.setOnClickListener {
            signInLauncher.launch(googleSignInClient.signInIntent)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmaillogin.text.toString()
            val password = binding.edtPasswd.text.toString()

            if (email.isEmpty()){
                binding.edtEmaillogin.error = "Email Harus Diisi!"
                binding.edtEmaillogin.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                binding.edtEmaillogin.error = "Email Tidak Valid!"
                binding.edtEmaillogin.requestFocus()
                return@setOnClickListener
            }


            if (password.isEmpty()){
                binding.edtPasswd.error = "Password Harus Diisi!"
                binding.edtPasswd.requestFocus()
                return@setOnClickListener
            }

            LoginFirebase(email,password)
            // viewModel.loginFirebase(firebaseAuth, email, password)
        }


    }

    private fun LoginFirebase(email: String, password: String,) {

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){
                if (it.isSuccessful){
                    Toast.makeText(this, "Selamat Datang $email", Toast.LENGTH_SHORT).show()
                    database.collection("Users").whereEqualTo("Email", email).get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                Log.d(TAG, "${document.get("NIK")}, ${document.get("Nama")}")

                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, exception.message.toString())
                        }

                    val intent = Intent(this,HomeActivity::class.java)
                    startActivity(intent)
                }
                else{
                    Toast.makeText(this, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }



    }


    override fun onStart() {
        super.onStart()
        val currentUser = firebaseAuth.currentUser
        updateUI(currentUser)
    }

    private fun setupAction() {
        binding.tvRegister.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
    companion object {
        private val TAG = LoginActivity::class.java.simpleName
    }
}