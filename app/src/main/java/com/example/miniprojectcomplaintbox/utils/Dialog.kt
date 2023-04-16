package com.example.miniprojectcomplaintbox.utils
import android.app.Activity
import android.app.AlertDialog
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import com.example.miniprojectcomplaintbox.R
import java.util.*


private lateinit var dialog: AlertDialog

fun initLoadingDialog(activity: Activity){
    val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
    val inflater: LayoutInflater = activity.layoutInflater
    builder.setView(inflater.inflate(R.layout.loading_dialog, null))
    builder.setCancelable(false)
    dialog = builder.create()
}

fun showDialog(){
    dialog.show()
}

fun dismissDialog(){
    dialog.dismiss()
}

fun getDateTime(s: Long): String? {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy - HH:mm")
        val netDate = Date(s)
        sdf.format(netDate)
    } catch (e: Exception) {
        e.toString()
    }
}