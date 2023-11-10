package com.example.steganography

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.steganography.databinding.ActivityLoginBinding

class loginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}