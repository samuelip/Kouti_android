package com.agile.kouti.quotation.subItem.sub_item

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.agile.kouti.R
import com.agile.kouti.db.quotation.SubItemQuotation
import com.agile.kouti.quotation.subItem.main_item.SubItemClickListener

class QuotationSubListAdapter(
    private val context: Context,
    private var subList: ArrayList<SubItemQuotation>,
    private val listener: SubItemClickListener): RecyclerView.Adapter<QuotationSubListAdapter.ViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_sub_item_list_quotation, parent, false)
        return QuotationSubListAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
       return subList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = subList[position]
        holder.tvNumberSub.text = (position+1).toString()

        holder.tvLocation.text = list.location
        holder.tvStock.text = list.stock_name

        holder.llHeaderSub.setOnClickListener {
            listener.onSubItemClick(list)
        }

        holder.ivSelectSub.setOnClickListener {
            listener.onItemSelectClick(list,position)
        }



        if(list.is_selected!!){
            holder.ivSelectSub.setImageResource(R.drawable.tickmark)
        }else
            holder.ivSelectSub.setImageResource(R.drawable.untickmark)

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNumberSub = itemView.findViewById(R.id.tvNumberSub) as AppCompatTextView
        val tvLocation = itemView.findViewById(R.id.tvLocation) as AppCompatTextView
        val tvStock = itemView.findViewById(R.id.tvStock) as AppCompatTextView
        val llHeaderSub = itemView.findViewById(R.id.llHeaderSub) as LinearLayout
        val ivSelectSub = itemView.findViewById(R.id.ivSelectSub) as ImageView
    }


}