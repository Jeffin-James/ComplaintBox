package com.example.miniprojectcomplaintbox

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniprojectcomplaintbox.databinding.ActivityViewDetailedComplaintBinding
import com.google.android.exoplayer2.ExoPlayer
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import com.example.miniprojectcomplaintbox.adapter.ImageViewRvAdapter
import com.example.miniprojectcomplaintbox.data.Complaint
import com.example.miniprojectcomplaintbox.utils.getDateTime
import com.google.android.exoplayer2.MediaItem
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import com.squareup.picasso.Picasso

class ViewDetailedComplaint : AppCompatActivity() {

    private lateinit var binding: ActivityViewDetailedComplaintBinding
    lateinit var currentUserId: String
    var isStaff : Boolean = false
    var district: String? = null
    private lateinit var sharedPref: SharedPreferences
    private lateinit var context : Context

    private lateinit var imageRvAdapter : ImageViewRvAdapter

    private val exoPlayer2 by lazy{
        ExoPlayer.Builder(this)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewDetailedComplaintBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.vvViewComplaint.player = exoPlayer2
        binding.ivViewComplaint.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        sharedPref = getSharedPreferences("Shared_Pref", AppCompatActivity.MODE_PRIVATE)
        district = sharedPref.getString("district", "")
        isStaff = sharedPref.getBoolean("isStaff", false)
        setValues()

    }

    private fun setValues(){

        FirebaseDatabase.getInstance("https://miniprojectcomplaintbox-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Complaints/${intent.getStringExtra("complaint_Id")}")
            .get().addOnSuccessListener {
                if (it.exists()){
                    setDataToView(it.getValue(Complaint::class.java)!!)
                    Log.d("Get Fun", "getComplaint: exists ")
                }
            }

        }

        private fun setDataToView(comp: Complaint) {
            val district = sharedPref.getString("district", "")
            val isStaff = sharedPref.getBoolean("isStaff", false)

            binding.tvViewComplaintTitle.text = comp.description
            binding.tvViewComplaintDate.text = getDateTime(comp.timeStamp!!)
            binding.tvViewComplaintLoc.text = comp.location
            binding.tvViewComplaintCount.text = comp.count.toString()
            binding.ivViewComplaint.visibility = View.GONE
            binding.viewImageFullScr.visibility = View.GONE
            binding.vvViewComplaint.visibility = View.GONE

            if (comp.imageUrlList != null && comp.imageUrlList!!.isNotEmpty()) {
                binding.ivViewComplaint.visibility = View.VISIBLE
                imageRvAdapter = ImageViewRvAdapter(
                    this,
                    urlList = comp.imageUrlList as MutableList<String>
                ) { url, position ->
                    binding.viewImageFullScr.visibility = View.VISIBLE
                    Picasso.get().load(url).into(binding.viewImageFullScr)
                }
                binding.ivViewComplaint.adapter = imageRvAdapter
            }
            if (comp.videoUrl != null && comp.videoUrl != "") {
                val video = MediaItem.fromUri(comp.videoUrl!!)
                binding.vvViewComplaint.visibility = View.VISIBLE
                exoPlayer2.setMediaItem(video)
                exoPlayer2.prepare()
                exoPlayer2.play()
            }

            binding.tvViewComplaintDistrict.text = comp.district
            binding.solved.visibility = View.GONE

            if (isStaff && district != "" && !comp.solved!!
            ) {
                binding.solved.visibility = View.VISIBLE

                binding.solved.setOnClickListener {
                    markSolved(comp.complaintId.toString())
                }

            }
        }

    private fun markSolved(complaintId: String) {
        FirebaseDatabase.getInstance("https://miniprojectcomplaintbox-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Complaints/$complaintId/solved")
            .setValue(true).addOnCompleteListener {

            }
    }



    override fun onBackPressed() {
            val intent = Intent(this,ViewComplaints::class.java)
            startActivity(intent)
        finish()
    }
}