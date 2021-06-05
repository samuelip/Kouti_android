package com.agile.kouti.payroll

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.agile.kouti.R
import com.agile.kouti.db.payroll.PayRoll
import com.agile.kouti.db.payroll.PayRollItem

class PayrollDetailsListAdapter(private val mContext: Context,
                                private var payrollList: ArrayList<PayRollItem>): RecyclerView.Adapter<PayrollDetailsListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.payroll_details_item, parent, false)
        return PayrollDetailsListAdapter.ViewHolder(v)
    }

    override fun getItemCount(): Int {
       return payrollList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = payrollList[position]

        holder.tvVendor.text = list.vendor
        holder.tvItem.text = list.item
        holder.tvRemark.text = list.remark
        holder.tvShop.text = list.shop
        holder.tvAmount.text = list.amount
    }

    class ViewHolder (itemView : View):RecyclerView.ViewHolder(itemView){
        val tvVendor = itemView.findViewById(R.id.tvVendor) as AppCompatTextView
        val tvItem = itemView.findViewById(R.id.tvItem) as AppCompatTextView
        val tvRemark = itemView.findViewById(R.id.tvRemark) as AppCompatTextView
        val tvShop = itemView.findViewById(R.id.tvShop) as AppCompatTextView
        val tvAmount = itemView.findViewById(R.id.tvAmount) as AppCompatTextView
    }
}