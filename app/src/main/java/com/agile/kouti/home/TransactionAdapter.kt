package com.agile.kouti.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.agile.kouti.R
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.setup_recycler_item.view.*

class TransactionAdapter(private val context: Context,
                         private val list: ArrayList<TransactionModel>,
                         private val listener: TransactionClickListener):RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.setup_recycler_item, parent, false)
        return TransactionAdapter.ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val listObj = list[position]

        holder.tvTitle.text = listObj.title
        Glide.with(context).load(listObj.image_drawable).placeholder(R.drawable.camera).into(holder.ivImage)

        holder.llRow.setOnClickListener{
            listener.onTransactionItemClick(listObj)
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {
        val tvTitle = itemView.findViewById(R.id.tvTitle) as AppCompatTextView
        val ivImage = itemView.findViewById(R.id.ivImage) as AppCompatImageView
        //val llRow = itemView.findViewById(R.id.llRow) as CardView

        val llRow = itemView.llRow
    }
}