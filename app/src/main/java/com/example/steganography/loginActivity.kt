package com.example.steganography

import android.content.Intent
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

            if(email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val loginIntent = Intent(this, MainActivity::class.java)
                        startActivity(loginIntent)
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }


            else
            {
                Toast.makeText(this, "Empty Fields Not Allowed", Toast.LENGTH_SHORT).show()
            }
        }



    }
}