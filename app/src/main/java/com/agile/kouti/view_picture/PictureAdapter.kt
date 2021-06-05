package com.agile.kouti.view_picture

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.agile.kouti.R
import com.agile.kouti.db.picture.PictureList
import com.bumptech.glide.Glide

class PictureAdapter(private val mContext: Context,
                     private val pictureList: ArrayList<PictureList>,
                     private val listener: OnPictureClickListener): RecyclerView.Adapter<PictureAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_picture_item, parent, false)
        return PictureAdapter.ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return pictureList.size

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = pictureList[position]

        Glide.with(mContext)
            .load(list.url)
            .placeholder(R.drawable.login_logo)
            .centerCrop()
            .into(holder.ivImage)

        holder.llRow.setOnClickListener{
            listener.onPictureItemClick(list)
        }
    }

    class ViewHolder (itemView : View):RecyclerView.ViewHolder(itemView) {
        val ivImage = itemView.findViewById(R.id.ivPictureImage) as AppCompatImageView
        val llRow = itemView.findViewById(R.id.llRowPicture) as CardView
    }


}