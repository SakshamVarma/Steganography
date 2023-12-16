package com.example.steganography

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.steganography.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class loginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.signUpText.setOnClickListener {
            val loginIntent = Intent(this, signupActivity::class.java)
            startActivity(loginIntent)
        }

        auth = FirebaseAuth.getInstance()
        binding.loginButton.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if(isOnline(this)){

            if (email.isNotEmpty() && password.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailEt.error = null
                binding.passwordEt.error = null
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                        val loginIntent = Intent(this, MainActivity::class.java)
                        startActivity(loginIntent)
                    } else {
                        //Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        Toast.makeText(
                            this,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()

                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
                }

            } else {
                if(email.isEmpty())
                    binding.emailEt.error = "Cannot be Empty"
                if(password.isEmpty())
                    binding.passwordEt.error = "Cannot be Empty"
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                {
                    binding.emailEt.error = "Invalid Email Address"
                }
            }
        }
            else{
                Toast.makeText(this, "You're Offline", Toast.LENGTH_SHORT).show()
            }
        }


    }

    override fun onStart() {
        super.onStart()

        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return true
            }
        }
        return false
    }
}