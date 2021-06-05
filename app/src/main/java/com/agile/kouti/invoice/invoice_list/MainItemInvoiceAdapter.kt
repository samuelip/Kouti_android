package com.agile.kouti.invoice.invoice_list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.agile.kouti.R
import com.agile.kouti.db.invoice.MainItemInvoice
import com.agile.kouti.db.quotation.MainItemQuotation
import com.agile.kouti.quotation.subItem.quotation.MainItemAdapter
import com.agile.kouti.quotation.subItem.quotation.MainItemClickListener

class MainItemInvoiceAdapter(
    private val context: Context,
    private var mainList: ArrayList<MainItemInvoice>,
    private val listener: MainItemInvoiceClickListener) : RecyclerView.Adapter<MainItemInvoiceAdapter.ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_main_item_invoice, parent, false)
        return MainItemInvoiceAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return  mainList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = mainList[position]

        holder.tvItemMain.text = list.item
        holder.tvRemarksMain.text = list.description

        holder.llHeaderMain.setOnClickListener {
            listener.onMainItemClick(list)
        }
    }

    class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvItemMain = itemView.findViewById(R.id.tvItemMainInv) as AppCompatTextView
        val tvRemarksMain = itemView.findViewById(R.id.tvRemarksMainInv) as AppCompatTextView
        val llHeaderMain = itemView.findViewById(R.id.llHeaderMainInv) as LinearLayout
    }


}