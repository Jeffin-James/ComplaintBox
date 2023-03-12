package com.example.miniprojectcomplaintbox

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.miniprojectcomplaintbox.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth
    lateinit var binding: ActivityLoginBinding

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser

        if(currentUser != null && binding.loginForm.visibility == View.VISIBLE){
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()

        auth = Firebase.auth

        binding.signupText.setOnClickListener {
            openRegister()
        }

        binding.LoginText.setOnClickListener {
            openLogin()
        }

        binding.loginButton.setOnClickListener {
            loginUser()
        }

        binding.registerButton.setOnClickListener {
            registerEmail()
        }


    }

    private fun openRegister() {
        binding.loginForm.visibility = View.GONE
        binding.registerForm.visibility = View.VISIBLE
    }
    private fun openLogin(){
        binding.registerForm.visibility = View.GONE
        binding.loginForm.visibility = View.VISIBLE
    }

    private fun registerEmail(){
        val email = binding.emailRegister.text.toString()
        val password = binding.passwordRegister.text.toString()
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter email and password",Toast.LENGTH_SHORT).show()
            return
        }
        else{
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener() { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(baseContext, "Registration Success",
                            Toast.LENGTH_SHORT).show()
                        binding.emailRegister.text = null
                        binding.passwordRegister.text = null
                    } else {
                        Toast.makeText(baseContext, "Registration Failed",
                            Toast.LENGTH_SHORT).show()
                    }
                }

        }

    }

    private fun loginUser(){
        val email = binding.emailLogin.text.toString()
        val password = binding.passwordLogin.text.toString()
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter email and password",Toast.LENGTH_SHORT).show()
            return
        }
        else{
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(baseContext, "Authentication Success",
                            Toast.LENGTH_SHORT).show()
                        val intent = Intent(this,MainActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }

        }

    }


}

