package com.example.steganography

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.steganography.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameLayout,EncryptFragment())
            .commit()

        auth = FirebaseAuth.getInstance()

        binding.encryptButton.setOnClickListener {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frameLayout,EncryptFragment())
                .commit()
        }
        binding.decryptButton.setOnClickListener {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frameLayout,DecryptFragment())
                .commit()
        }

        binding.listButton.setOnClickListener {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frameLayout,ListFragment())
                .commit()
        }

        binding.logOutButton.setOnClickListener {
            logOutDialog()
        }
    }
    private fun logOutDialog()
    {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setMessage("Do you wish to Sign out?")
            .setPositiveButton("Yes"){ _, _ ->
                auth.signOut()
                startActivity(Intent(this, loginActivity::class.java))
                Toast.makeText(this, "Successfully Logged Out", Toast.LENGTH_LONG).show()
                this.finish()
            }
            .setNegativeButton("No"){ _, _ ->

            }
            .show()
    }
}