package com.agile.kouti.quotation.subItem.main_item

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.agile.kouti.R
import com.agile.kouti.crm.PersonAdapter
import com.agile.kouti.db.quotation.SubItemQuotation

class SubItemAdapter(
    private val context: Context,
    private var subList: ArrayList<SubItemQuotation>,
    private val listener: SubItemClickListener): RecyclerView.Adapter<SubItemAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_sub_item_quotation, parent, false)
        return SubItemAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return  subList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = subList[position]

        holder.tvStock.text = list.stock_name
        holder.tvLocation.text = list.location

        holder.llHeader.setOnClickListener {
            listener.onSubItemClick(list)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStock = itemView.findViewById(R.id.tvStock) as AppCompatTextView
        val tvLocation = itemView.findViewById(R.id.tvLocation) as AppCompatTextView
        val llHeader = itemView.findViewById(R.id.llHeader) as LinearLayout
    }


}