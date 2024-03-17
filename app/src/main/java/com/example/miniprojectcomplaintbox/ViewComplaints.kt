package com.example.miniprojectcomplaintbox

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.content.SharedPreferences
import android.content.Context
import android.content.Intent
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniprojectcomplaintbox.adapter.UserRvAdapter
import com.example.miniprojectcomplaintbox.data.Complaint
import com.example.miniprojectcomplaintbox.databinding.ActivityViewComplaintsBinding
import com.example.miniprojectcomplaintbox.notification.FirebaseService.Companion.sharedPref
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ViewComplaints : AppCompatActivity(), UserRvAdapter.CardClickListener {

    private lateinit var binding: ActivityViewComplaintsBinding
    private lateinit var rvAdapter: UserRvAdapter
    var compList: MutableLiveData<List<Complaint?>?> = MutableLiveData()
    lateinit var currentUserId: String
    var isStaff : Boolean = false
    var district: String? = null
    var count: Long = 0
    private lateinit var sharedPref : SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewComplaintsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getFirebaseData()
        binding.viewAllComplaints.layoutManager = LinearLayoutManager(applicationContext)
        sharedPref = getSharedPreferences("Shared_Pref", MODE_PRIVATE)
        district = sharedPref!!.getString("district","")
        count = sharedPref!!.getLong("count",0)
        Log.d("CountNum",count.toString())
        compList.observe(this) { list ->
            var cList = if (district != null && district!!.isNotEmpty()) {
                list?.filter {
                    it?.district?.contains(district!!)!! && (it?.count!! >= count)
                }
            } else {
                list?.filter { it?.userId.equals(currentUserId) }
            }
            rvAdapter = UserRvAdapter(this,cList, this)
            binding.viewAllComplaints.adapter = rvAdapter
        }
    }


    private fun getFirebaseData() {

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        FirebaseDatabase.getInstance("https://miniprojectcomplaintbox-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Complaints")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Complaint?>()
                    if (snapshot.exists()) {
                        for (c in snapshot.children) {
                            Log.d("TAGinController", "onDataChange: ${c.key}")
                            list.add(c.getValue(Complaint::class.java))
                        }
                        compList.postValue(list)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
//                    TODO("Not yet implemented")
                }
            })
    }

    override fun onCardClick(comp: Complaint) {
        val intent = Intent(this,ViewDetailedComplaint::class.java)
        intent.putExtra("complaint_Id",comp.complaintId)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
        finish()
    }



}


