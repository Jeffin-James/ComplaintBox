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
    private lateinit var context : Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewComplaintsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewAllComplaints.layoutManager = LinearLayoutManager(applicationContext)
        sharedPref = context.getSharedPreferences("Shared_Pref", MODE_PRIVATE)
        district = sharedPref.getString()
        compList.observe(this) { list ->
            var cList = if (district != null && district!!.isNotEmpty()) {
                list?.filter {
                    it?.district?.contains(district!!)!!
                }
            } else {
                list?.filter { it?.userId.equals(currentUserId) }
            }
            rvAdapter = UserRvAdapter(applicationContext, cList, )
            binding.viewAllComplaints.adapter = rvAdapter
        }
        getFirebaseData()

    }


    private fun getFirebaseData() {

        FirebaseDatabase.getInstance().getReference("Complaints")
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
        intent.putExtra("complaint",comp)
        startActivity(intent)

    }


}


