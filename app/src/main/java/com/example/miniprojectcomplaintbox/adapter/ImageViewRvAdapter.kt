package com.example.miniprojectcomplaintbox.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.miniprojectcomplaintbox.databinding.RvImageViewBinding
import com.squareup.picasso.Picasso

class ImageViewRvAdapter (
    private val context: Context,
    private val uriList: MutableList<Uri> ?= null,
    private var urlList : MutableList<String> ?= null,
    private val onClick: (String, Int) -> Unit
    ) : RecyclerView.Adapter<ImageViewRvAdapter.ViewHolder>(){

    private lateinit var binding: RvImageViewBinding

    class ViewHolder(view: View) :RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = RvImageViewBinding.inflate(LayoutInflater.from(context), parent , false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(uriList != null){
            Picasso.get()
                .load(uriList[position])
                .fit().centerCrop()
                .into(binding.rvImageView)
        } else if(urlList != null) {
            Picasso.get()
                .load(urlList?.get(position))
                .fit().centerCrop()
                .into(binding.rvImageView)
            binding.root.setOnClickListener {
                onClick.invoke(urlList!![position],position)
            }
        }
    }

    override fun getItemCount(): Int {
        return uriList?.size ?: urlList!!.size
    }

    fun addImageUrlList(url: String){
        urlList!!.add(url)
        notifyDataSetChanged()
    }

    fun clearUrlList(){
        urlList!!.clear()
        notifyDataSetChanged()
    }

    fun clearUriList(){
        uriList!!.clear()
        notifyDataSetChanged()
    }


}