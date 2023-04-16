package com.example.miniprojectcomplaintbox

import android.Manifest
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniprojectcomplaintbox.adapter.ImageViewRvAdapter
import com.example.miniprojectcomplaintbox.data.Complaint
import com.example.miniprojectcomplaintbox.data.LoginData
import com.example.miniprojectcomplaintbox.databinding.ActivityAddComplaintsBinding
import com.example.miniprojectcomplaintbox.notification.NotificationData
import com.example.miniprojectcomplaintbox.notification.PushNotification
import com.example.miniprojectcomplaintbox.notification.RetrofitInstance
import com.example.miniprojectcomplaintbox.utils.initLoadingDialog
import com.google.android.exoplayer2.ExoPlayer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

class AddComplaints : AppCompatActivity() {

    private lateinit var binding : ActivityAddComplaintsBinding
    private var dataCode: Int = 0
    private var videoUri: Uri? = null
    private var videoUrl = ""
    private lateinit var uriList: MutableList<Uri>
    private val imageUrlList = mutableListOf<String>()
    private var district = ""

    private val exoPlayer2 by lazy {
        ExoPlayer.Builder(this)
            .build()
    }
    private val imageViewRvAdapter by lazy {
        ImageViewRvAdapter(this, uriList){_,_->}
    }

    private val getImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK && it.data != null) {
            if (dataCode == 1) {
                val thumbnail: Bitmap = it.data!!.extras!!.get("data") as Bitmap
                binding.rvComplaint.visibility = View.VISIBLE
                uriList.add(getImageUri(thumbnail))
                imageViewRvAdapter.notifyDataSetChanged()
            } else if (dataCode == 2) {
                binding.vvComplaint.visibility = View.VISIBLE
                binding.vvComplaint.setVideoURI(it.data!!.data)
                binding.vvComplaint.start()
                videoUri = it.data!!.data
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddComplaintsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initImageViewRecyclerView()
        clickListener()
        initLoadingDialog(this)
    }

    private fun initImageViewRecyclerView() {
        uriList = mutableListOf()
        binding.rvComplaint.layoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        binding.rvComplaint.adapter = imageViewRvAdapter
    }

    private fun clickListener(){
        binding.addImage.setOnClickListener {
            checkForPermission(1)
        }

        binding.addVideo.setOnClickListener {
            checkForPermission(2)
        }
        binding.submit.setOnClickListener {
            extractData()
        }
    }

    private fun extractData() {
        if (binding.etDistrict.text.toString().isNotEmpty() && binding.etComplaintDescription.text.toString().isNotEmpty()
            && binding.etLocation.text.toString().isNotEmpty()
        ) {
            initStorageProcess()
        } else {
            Toast.makeText(this, "Fill all the fields!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun initStorageProcess() {
        com.example.miniprojectcomplaintbox.utils.showDialog()
        if (uriList.size > 0 && videoUri != null) {
            storeImage()
        } else if (uriList.size > 0) {
            storeImage()
        } else if (videoUri != null) {
            storeVideoToCloud()
        } else {
            storeInDatabase()
        }
    }

    private fun storeImage() {
        uriList.forEach {
            FirebaseStorage.getInstance()
                .getReference("File/${Calendar.getInstance().timeInMillis}")
                .putFile(it).addOnSuccessListener { ivSnap ->
                    ivSnap.storage.downloadUrl.addOnSuccessListener { url ->
                        imageUrlList.add(url.toString())
                        if (videoUri != null && imageUrlList.size == uriList.size)
                            storeVideoToCloud()
                        else if (imageUrlList.size == uriList.size)
                            storeInDatabase()
                    }
                }.addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Failed to post the data please retry!!",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun storeInDatabase() {
        val description = binding.etComplaintDescription.text.toString().trim()
        district = binding.etDistrict.text.toString().trim()
        val loc = binding.etLocation.text.toString().trim()
        val time = Calendar.getInstance().timeInMillis
        var vUrl = ""

        if (videoUrl.isNotEmpty())
            vUrl = videoUrl
        val complaintId = FirebaseDatabase.getInstance().getReference("Complaints")
            .push().key

        val complaint = Complaint(
            videoUrl = vUrl,
            description = description,
            district = district,
            timeStamp = time,
            userId = FirebaseAuth.getInstance().uid.toString(),
            location = loc,
            complaintId = complaintId,
            imageUrlList = imageUrlList
        )

        FirebaseDatabase.getInstance().getReference("Complaints/$complaintId")
            .setValue(complaint)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    com.example.miniprojectcomplaintbox.utils.dismissDialog()
                    getNotificationToken()
                }
            }
    }

    private fun getNotificationToken() {
        for (dis in district) {
            FirebaseDatabase.getInstance().getReference("Staff/$dis")
                .get().addOnSuccessListener {
                    for (children in it.children) {
                        val userData = children.getValue(LoginData::class.java)
                        if (userData!!.token != null && userData.token != "")
                            setNotificationToken(userData.token!!)
                    }
                }
        }

    }

    private fun checkForPermission(code: Int) {
        Dexter.withContext(this)
            .withPermissions(listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    if (p0!!.areAllPermissionsGranted()) {
                        if (code == 1) {
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            dataCode = 1
                            getImage.launch(intent)
                            Toast.makeText(
                                applicationContext,
                                "All permission checked",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                            dataCode = 2
                            getImage.launch(intent)
                        }

                    } else {
                        showRationalDialogForPermissions()
                    }

                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }

            }).onSameThread().check()
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this).setMessage("Please enable the required permissions")
            .setPositiveButton("GO TO SETTINGS")
            { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel")
            { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun storeVideoToCloud() {
        FirebaseStorage.getInstance()
            .getReference("File/${Calendar.getInstance().timeInMillis}")
            .putFile(videoUri!!).addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { url ->
                    videoUrl = url.toString()
                    storeInDatabase()
                }
            }
    }

    private fun getImageUri(inImage: Bitmap): Uri {
        val tempFile = File.createTempFile("temprentpk", ".png")
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes)
        val bitmapData = bytes.toByteArray()

        val fileOutPut = FileOutputStream(tempFile)
        fileOutPut.write(bitmapData)
        fileOutPut.flush()
        fileOutPut.close()
        return Uri.fromFile(tempFile)
    }
    private fun closeKeyboard() {
        val view: View = this.currentFocus!!
        val manager: InputMethodManager? =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        manager?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setNotificationToken(token: String) {
        PushNotification(
            NotificationData("Got an problem", "Some one has posted an problem click to view."),
            token
        ).also {
            sendNotification(it)
        }
    }

    private fun sendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.postNotification(notification)
                if (response.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Notification has been sent successfully",
                        Toast.LENGTH_LONG
                    ).show()

                } else {
                    Toast.makeText(applicationContext, "Error occured", Toast.LENGTH_LONG).show()

                }
            } catch (e: Exception) {
                Log.e("TAGData", e.toString())
            }
        }
}