package com.example.miniprojectcomplaintbox

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.miniprojectcomplaintbox.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()

        auth = Firebase.auth
        val currentUser = auth.currentUser
        if (currentUser != null) {
            binding.username.text = currentUser.email.toString()
        }
        else{
            goToLogin()
        }
        binding.signout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            goToLogin()
        }

    }
    private fun goToLogin(){
        val intent = Intent(applicationContext,LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}