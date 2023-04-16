package com.example.miniprojectcomplaintbox

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniprojectcomplaintbox.databinding.ActivityViewDetailedComplaintBinding
import com.google.android.exoplayer2.ExoPlayer
import android.content.Context
import android.view.View
import com.example.miniprojectcomplaintbox.adapter.ImageViewRvAdapter
import com.example.miniprojectcomplaintbox.data.Complaint
import com.example.miniprojectcomplaintbox.utils.getDateTime
import com.google.android.exoplayer2.MediaItem
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class ViewDetailedComplaint : AppCompatActivity() {

    private lateinit var binding: ActivityViewDetailedComplaintBinding
    lateinit var currentUserId: String
    var isStaff : Boolean = false
    var district: String? = null
    private lateinit var sharedPref: SharedPreferences
    private lateinit var context : Context
    private lateinit var comp : Complaint
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
        binding.ivViewComplaint.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
        sharedPref = context.getSharedPreferences("Shared_Pref", AppCompatActivity.MODE_PRIVATE)
        district = sharedPref.getString("district", "")
        isStaff = sharedPref.getBoolean("isStaff", false)

    }

    private fun setValues(){
        val district = comp.district
        val isStaff = sharedPref.getBoolean("isStaff",false)

        if (binding != null){
            binding.tvViewComplaintTitle.text = comp.description
            binding.tvViewComplaintDate.text = getDateTime(comp.timeStamp!!)
            binding.tvViewComplaintLoc.text =  comp.location
            binding.ivViewComplaint.visibility = View.GONE
            binding.viewImageFullScr.visibility = View.GONE
            binding.vvViewComplaint.visibility = View.GONE

            if (comp.imageUrlList != null && comp.imageUrlList!!.isNotEmpty()) {
                binding.ivViewComplaint.visibility = View.VISIBLE
                imageRvAdapter = ImageViewRvAdapter(
                    context,
                    urlList = comp.imageUrlList as MutableList<String>){url, position ->
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
    }
    private fun markSolved(complaintId: String) {
        FirebaseDatabase.getInstance().getReference("Complaints/$complaintId/solved")
            .setValue(true).addOnCompleteListener {

            }
    }
}