package com.example.miniprojectcomplaintbox

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.miniprojectcomplaintbox.data.KeyData
import com.example.miniprojectcomplaintbox.databinding.ActivityLoginBinding
import com.example.miniprojectcomplaintbox.model.UserData
import com.example.miniprojectcomplaintbox.utils.initLoadingDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth
    lateinit var binding: ActivityLoginBinding
    private lateinit var phoneNumber : String
    private lateinit var verificationID : String
    private lateinit var district : String
    private var count: Long? = null
    private lateinit var database : DatabaseReference
    private lateinit var currentUserId : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()
        initLoadingDialog(this)
        auth = FirebaseAuth.getInstance()
        clickListener()
    }

    private fun clickListener(){
        binding.getOTPButton.setOnClickListener {
            getOTP()
        }

        binding.verifyOTPButton.setOnClickListener {
            verifyOtp()
        }

        binding.nextButton.setOnClickListener {
            storeUserData()
        }

        binding.iAmStaff.setOnClickListener {
            openStaffLogin()
        }

        binding.staffLoginButton.setOnClickListener {
            loginStaff()
        }

    }

    private fun loginStaff() {
        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(binding.enterEmail.text.toString(), binding.enterPassword.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    verifyUser()
                    Log.d("TAGData", "createUserWithEmail:success")
                } else {
                    Log.d("TAGData", "createUserWithEmail:failure")
                    Toast.makeText(this, "Failure", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun verifyUser() {
        FirebaseDatabase.getInstance("https://miniprojectcomplaintbox-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("StaffKey/${FirebaseAuth.getInstance().uid}")
            .get()
            .addOnSuccessListener {
                if (it.exists()) {
                    val keyData = it.getValue(KeyData::class.java)
                    keyData!!.count?.let { it1 ->
                        getSharedPreferences("Shared_Pref", MODE_PRIVATE).edit()
                            .putBoolean("isStaff", true)
                            .putString("district", keyData!!.district.toString())
                            .putString("name",keyData!!.name.toString())
                            .putLong("count", it1)
                            .apply()
                    }
                    currentUserId = FirebaseAuth.getInstance().uid.toString()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    Log.d("TAGData", "getData: ${keyData.district}")
                    Log.d("TAGData", "getData: ${keyData.count}")
                    Log.d("TAGData", "getData: ${keyData.name}")
                } else {
                    Toast.makeText(this, "Make sure you are a staff", Toast.LENGTH_LONG)
                        .show()
                    FirebaseAuth.getInstance().signOut()
                }

            }
            .addOnFailureListener {
                //district = getDistrict()
                //count = getCount()
                FirebaseDatabase.getInstance("https://miniprojectcomplaintbox-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("StaffKey/${FirebaseAuth.getInstance().uid.toString()}")
                    .setValue(
                        KeyData(
                            district = district,
                            userId = FirebaseAuth.getInstance().uid.toString(),
                            count = count
                        )
                    ).addOnSuccessListener {
                        getSharedPreferences("Shared_Pref", MODE_PRIVATE).edit()
                            .putBoolean("isStaff", true)
                            .putString("district", district)
                            .putLong("count", count!!)
                            .apply()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }

            }
    }

    private fun openStaffLogin(){
        binding.sendOTPForm.visibility = View.GONE
        binding.staffLogin.visibility = View.VISIBLE
    }
    private fun getOTP(){
        com.example.miniprojectcomplaintbox.utils.showDialog()
        phoneNumber = binding.phoneNumber.text.toString().trim()
        if(!phoneNumber.isEmpty() && phoneNumber.length == 10){
            phoneNumber = "+91$phoneNumber"
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
        else{
            Toast.makeText(this,"Please enter phone number",Toast.LENGTH_SHORT).show()
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            com.example.miniprojectcomplaintbox.utils.dismissDialog()
        }

        override fun onVerificationFailed(e: FirebaseException) {

            if (e is FirebaseAuthInvalidCredentialsException) {
                com.example.miniprojectcomplaintbox.utils.dismissDialog()
                Log.d("CheckMobile", "onVerificationFailed: ${e.message} ")
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            }
            else if (e is FirebaseTooManyRequestsException) {
                com.example.miniprojectcomplaintbox.utils.dismissDialog()
                Log.d("CheckMobile", "onVerificationFailed: ${e.message} ")

                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            }

            com.example.miniprojectcomplaintbox.utils.dismissDialog()
            Log.d("CheckMobile", "onVerificationFailed: ${e.message} ")

            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            com.example.miniprojectcomplaintbox.utils.dismissDialog()
            verificationID = verificationId
            binding.sendOTPForm.visibility = View.GONE
            binding.enterOTPForm.visibility = View.VISIBLE
            Toast.makeText(applicationContext, "Code sent!!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verifyOtp() {
        com.example.miniprojectcomplaintbox.utils.showDialog()
        if (verificationID.isNotEmpty() && binding.enterOTP.text.toString().isNotEmpty()) {
            val phoneAuthCredential = PhoneAuthProvider.getCredential(
                verificationID, binding.enterOTP.text.toString().trim()
            )
            signInWithPhoneAuthCredential(phoneAuthCredential)
        } else {
            com.example.miniprojectcomplaintbox.utils.dismissDialog()
            Snackbar.make(binding.root, "Check the OTP", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    com.example.miniprojectcomplaintbox.utils.dismissDialog()
                    checkDatabase()
                } else {
                    com.example.miniprojectcomplaintbox.utils.dismissDialog()
                }
            }
            .addOnFailureListener{
                com.example.miniprojectcomplaintbox.utils.dismissDialog()
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            }
    }

    private fun checkDatabase() {
        FirebaseDatabase.getInstance(Utils.DB_URL).getReference("UserData/${FirebaseAuth.getInstance().uid}")
            .get()
            .addOnSuccessListener {
                if (it.exists()){
                    val userData = it.getValue(UserData::class.java)
                    startActivity(Intent(this, MainActivity::class.java))
                    Toast.makeText(this, "Verified Successfully", Toast.LENGTH_LONG).show()
                    finish()
                }
                else{
                    showEnterDetails()
                }
            }
    }

    private fun showEnterDetails(){
        binding.enterOTPForm.visibility = View.GONE
        binding.detailsForm.visibility = View.VISIBLE
    }


    private fun storeUserData(){
        database = FirebaseDatabase.getInstance("https://miniprojectcomplaintbox-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("UserData")
        database.child(FirebaseAuth.getInstance().uid.toString()).setValue(
                UserData(
                    binding.enterName.text.toString(),
                    phoneNumber,
                    binding.enterDistrict.text.toString()
                )
            )
            .addOnSuccessListener {
                startActivity(Intent(this,MainActivity::class.java))
                Toast.makeText(this, "Verified Successfully", Toast.LENGTH_LONG).show()
                finish()
            }
    }

    private fun getDistrict(): String {
        binding.staffLogin.visibility = View.GONE
        binding.detailsForm.visibility = View.VISIBLE
        binding.enterName.visibility = View.GONE
        return binding.enterDistrict.text.toString().trim()
    }


    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}



