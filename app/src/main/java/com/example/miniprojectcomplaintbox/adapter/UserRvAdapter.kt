package com.example.miniprojectcomplaintbox.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.miniprojectcomplaintbox.data.Complaint
import com.example.miniprojectcomplaintbox.R
import com.example.miniprojectcomplaintbox.utils.getDateTime
import com.squareup.picasso.Picasso

class UserRvAdapter(
    private val context: Context,
    private val compList: List<Complaint?>?,
    private val clickListener: CardClickListener
): RecyclerView.Adapter<UserRvAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val titleView: TextView = itemView.findViewById(R.id.card_title)
        val locationView: TextView = itemView.findViewById(R.id.card_loc)
        val districtView: TextView = itemView.findViewById(R.id.card_dist)
        val timeView: TextView = itemView.findViewById(R.id.card_time)
        val imageView: ImageView = itemView.findViewById(R.id.card_image)
        val statusLL: LinearLayout = itemView.findViewById(R.id.comp_status_indicator)
        val card: CardView = itemView.findViewById(R.id.comp_card)
        val videoStatus: TextView = itemView.findViewById(R.id.card_videoStatus)
        val count : TextView = itemView.findViewById(R.id.card_count)

    }

    interface CardClickListener {
        fun onCardClick(comp: Complaint)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserRvAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.complaint_card,parent,false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: UserRvAdapter.ViewHolder, position: Int) {
        val comp = compList?.get(position)
        holder.videoStatus.visibility = View.GONE
        holder.titleView.text = comp?.description
        holder.locationView.text= comp?.location
        holder.itemView.setOnClickListener {
            clickListener.onCardClick(comp!!)
        }
        holder.districtView.text = comp?.district
        holder.count.text = comp?.count.toString()
        holder.timeView.text = getDateTime(comp?.timeStamp!!)
        if (comp.solved!!) {
            holder.statusLL.setBackgroundColor(context.getColor(R.color.lawn_green))
            holder.card.setCardBackgroundColor(context.getColor(R.color.green_cardBG))
        }
        if (comp.imageUrlList != null && comp.imageUrlList.isNotEmpty()) {
            Picasso.get().load(comp.imageUrlList[0])
                .fit()
                .centerInside()
                .into(holder.imageView)
        }
        if (comp?.videoUrl != null && comp?.videoUrl != "" ) {
            holder.videoStatus.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = compList?.size!!
}