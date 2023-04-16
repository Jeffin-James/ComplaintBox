package com.example.miniprojectcomplaintbox

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import com.example.miniprojectcomplaintbox.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        drawerToggle = ActionBarDrawerToggle(this,binding.drawerLayout,R.string.open,R.string.close)
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.home -> Toast.makeText(applicationContext,"Home Selected",Toast.LENGTH_SHORT).show()
                R.id.addComplaint -> Toast.makeText(applicationContext,"Add Complaint Selected",Toast.LENGTH_SHORT).show()
                R.id.viewComplaint -> Toast.makeText(applicationContext,"View Complaint Selected",Toast.LENGTH_SHORT).show()
                R.id.viewProfile -> Toast.makeText(applicationContext,"Profile Selected",Toast.LENGTH_SHORT).show()
                R.id.about -> Toast.makeText(applicationContext,"About Selected",Toast.LENGTH_SHORT).show()
                R.id.help -> Toast.makeText(applicationContext,"Help Selected",Toast.LENGTH_SHORT).show()
                R.id.signOut -> goToLogin()
            }
            true
        }




    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(drawerToggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    private fun goToLogin(){
        val intent = Intent(applicationContext,LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}