package com.example.miniprojectcomplaintbox

import android.content.ClipData.Item
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import com.example.miniprojectcomplaintbox.data.LoginData
import com.example.miniprojectcomplaintbox.databinding.ActivityMainBinding
import com.example.miniprojectcomplaintbox.notification.FirebaseService
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var sharedPref : SharedPreferences
    private var isStaff : Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        sharedPref = getSharedPreferences("Shared_Pref", MODE_PRIVATE)
        isStaff = sharedPref.getBoolean("isStaff",false)
        if(isStaff){

            saveNotificationToken()
            binding.addComplaintIcon.visibility = View.GONE
        }
        clickListener()
    }

    private fun goToLogin(){
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(applicationContext,LoginActivity::class.java)
        startActivity(intent)
        sharedPref.edit().clear().apply()
        finish()
    }

    private fun saveNotificationToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val loginData = LoginData(FirebaseAuth.getInstance().uid.toString(), token)
            FirebaseDatabase.getInstance("https://miniprojectcomplaintbox-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Staff/${sharedPref.getString("district",null)}/${FirebaseAuth.getInstance().uid}")
                .setValue(loginData)
        }
    }




    private fun addComplaint(){
        val intent = Intent(this,AddComplaints::class.java)
        startActivity(intent)
        finish()
    }

    private fun viewComplaint(){
        val intent = Intent(this,ViewComplaints::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToHome(){
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun clickListener(){
        binding.addComplaintIcon.setOnClickListener{
            addComplaint()
        }
        binding.viewComplaintIcon.setOnClickListener {
            viewComplaint()
        }
        binding.signOutIcon.setOnClickListener {
            goToLogin()
        }
    }

}