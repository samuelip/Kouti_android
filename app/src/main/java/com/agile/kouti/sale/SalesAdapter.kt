package com.agile.kouti.sale

import android.content.Context
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.agile.kouti.R

class SalesAdapter(private val context: Context,
                   private val salesList: ArrayList<SalesModel>,
                   private val listener:OnItemClickListener):RecyclerView.Adapter<SalesAdapter.ViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.sales_item,parent,false)
        return SalesAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return salesList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = salesList[position]
        holder.tvTitle.text = list.title

        holder.llRow.setOnClickListener{
            listener.onItemClick(list,position)
        }

        if(position == (salesList.size-1))
            holder.view.visibility = View.GONE
    }

    class ViewHolder(itemView : View):RecyclerView.ViewHolder(itemView) {
        val tvTitle = itemView.findViewById(R.id.tvTitle) as AppCompatTextView
        val llRow = itemView.findViewById(R.id.llRow) as LinearLayout
        val view = itemView.findViewById(R.id.view) as View
    }

}