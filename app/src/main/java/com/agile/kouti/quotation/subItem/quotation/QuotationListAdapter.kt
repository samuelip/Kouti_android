package com.agile.kouti.quotation.subItem.quotation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.agile.kouti.R
import com.agile.kouti.crm.CrmClickListener
import com.agile.kouti.crm.CrmListAdapter
import com.agile.kouti.db.crm.Crm
import com.agile.kouti.db.quotation.Quotation

class QuotationListAdapter (private val mContext: Context,
                            private var quotationList: ArrayList<Quotation>,
                            private val listener: QuotationClickListener): RecyclerView.Adapter<QuotationListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.quotation_item_row, parent, false)
        return QuotationListAdapter.ViewHolder(v)
    }

    override fun getItemCount(): Int {
       return quotationList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var list = quotationList[position]
        holder.tvNumber.text = (position+1).toString()
        holder.tvDate.text = list.date
        holder.tvStatus.text = list.status
        holder.tvQuotationNo.text = list.quotation_no
        holder.tvMainListCount.text = "("+list.total_main_item+") Main Item"

        holder.llRowQuotation.setOnClickListener {
            listener.onQuotationItemClick(list)
        }

        holder.ivSelectQuo.setOnClickListener {
            listener.onItemSelectClick(list,position)
        }

        holder.llMainListCount.setOnClickListener {
            listener.onMainItemSelectClick(list,position)
        }

        holder.ivSelectQuo.setOnClickListener {
            listener.onItemSelectClick(list,position)
        }

        if(list.is_selected!!){
            holder.ivSelectQuo.setImageResource(R.drawable.tickmark)
        }else
            holder.ivSelectQuo.setImageResource(R.drawable.untickmark)
    }

    fun filterList(filteredList: ArrayList<Quotation>) {
        this.quotationList = filteredList
        notifyDataSetChanged();
    }

    class ViewHolder (itemView : View):RecyclerView.ViewHolder(itemView) {
        val tvNumber = itemView.findViewById(R.id.tvNumberQuo) as AppCompatTextView
        val tvDate = itemView.findViewById(R.id.tvDateQuo) as AppCompatTextView
        val tvStatus = itemView.findViewById(R.id.tvStatusQuo) as AppCompatTextView
        val tvQuotationNo = itemView.findViewById(R.id.tvQuotationNo) as AppCompatTextView
        val tvMainListCount = itemView.findViewById(R.id.tvMainListCount) as AppCompatTextView
        val ivSelectQuo = itemView.findViewById(R.id.ivSelectQuo) as ImageView
        val llRowQuotation = itemView.findViewById(R.id.llRowQuotation) as LinearLayout
        val llMainListCount = itemView.findViewById(R.id.llMainListCount) as LinearLayout
    }


}