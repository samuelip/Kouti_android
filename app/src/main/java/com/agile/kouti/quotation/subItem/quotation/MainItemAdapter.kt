package com.agile.kouti.quotation.subItem.quotation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.agile.kouti.R
import com.agile.kouti.db.quotation.MainItemQuotation
import com.agile.kouti.quotation.subItem.main_item.SubItemAdapter

class MainItemAdapter(
    private val context: Context,
    private var mainList: ArrayList<MainItemQuotation>,
    private val listener: MainItemClickListener): RecyclerView.Adapter<MainItemAdapter.ViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_main_item_quotation, parent, false)
        return MainItemAdapter.ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = mainList[position]

        holder.tvItemMain.text = list.item
        holder.tvRemarksMain.text = list.description

        holder.llHeaderMain.setOnClickListener {
            listener.onMainItemClick(list)
        }
    }

    override fun getItemCount(): Int {
        return  mainList.size
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItemMain = itemView.findViewById(R.id.tvItemMain) as AppCompatTextView
        val tvRemarksMain = itemView.findViewById(R.id.tvRemarksMain) as AppCompatTextView
        val llHeaderMain = itemView.findViewById(R.id.llHeaderMain) as LinearLayout
    }

}