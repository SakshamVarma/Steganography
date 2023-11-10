package com.example.steganography

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.steganography.databinding.ActivitySignupBinding

class signupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}