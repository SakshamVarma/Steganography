package com.example.steganography

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.steganography.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth

class signupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //disable Signup Button
//        binding.signupButton.isEnabled = false
//        binding.signupButton.isClickable = false

        binding.loginText.setOnClickListener {
            val loginIntent = Intent(this, loginActivity::class.java)
            startActivity(loginIntent)
        }

        auth = FirebaseAuth.getInstance()
        binding.signupButton.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            val confirm_password = binding.editTextRePassword.text.toString().trim()

            if(email.isNotEmpty() && password.isNotEmpty() && confirm_password.isNotEmpty())
            {
                if (password == confirm_password){
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                        if(it.isSuccessful){
                            val loginIntent = Intent(this, loginActivity::class.java)
                            startActivity(loginIntent)
                        }
                        else{
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else{
                    Toast.makeText(this, "Password Doesn't Match", Toast.LENGTH_SHORT).show()
                }
            }
            else
            {
                Toast.makeText(this, "Empty Fields Not Allowed", Toast.LENGTH_SHORT).show()
            }
        }


    }
}