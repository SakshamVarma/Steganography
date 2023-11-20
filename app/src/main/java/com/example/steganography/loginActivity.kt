package com.example.steganography

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.steganography.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class loginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //disable Login Button
//        binding.loginButton.isEnabled = false
//        binding.loginButton.isClickable = false

        binding.signUpText.setOnClickListener {
            val loginIntent = Intent(this, signupActivity::class.java)
            startActivity(loginIntent)
        }

        auth = FirebaseAuth.getInstance()
        binding.loginButton.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if(isOnline(this)){

            if (email.isNotEmpty() && password.isNotEmpty()) {
                binding.emailEt.error = null
                binding.passwordEt.error = null
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val loginIntent = Intent(this, MainActivity::class.java)
                        startActivity(loginIntent)
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                if(email.isEmpty())
                    binding.emailEt.error = "Cannot be Empty"
                if(password.isEmpty())
                    binding.passwordEt.error = "Cannot be Empty"
                //Toast.makeText(this, "Empty Fields Not Allowed", Toast.LENGTH_SHORT).show()
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