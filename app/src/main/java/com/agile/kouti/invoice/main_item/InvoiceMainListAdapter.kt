package com.agile.kouti.invoice.main_item

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.agile.kouti.R
import com.agile.kouti.db.invoice.MainItemInvoice
import com.agile.kouti.db.quotation.MainItemQuotation
import com.agile.kouti.invoice.invoice_list.MainItemInvoiceClickListener
import com.agile.kouti.quotation.subItem.main_item.QuotationMainListAdapter

class InvoiceMainListAdapter  (
    private val context: Context,
    private var mainList: ArrayList<MainItemInvoice>,
    private val listener: MainItemInvoiceClickListener): RecyclerView.Adapter<InvoiceMainListAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_main_item_list_invoice, parent, false)
        return InvoiceMainListAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
       return  mainList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = mainList[position]


        holder.tvNumberMain.text = (position+1).toString()
        holder.tvItemMain.text = list.item
        holder.tvRemarksMain.text = list.description

        holder.tvSubStockCount.text = "("+list.total_sub_item+") Sub Stock Item"
        //holder.tvSubStockCount.text = list.stock_list

        holder.llHeaderMain.setOnClickListener {
            listener.onMainItemClick(list)
        }

        holder.llMainListCount.setOnClickListener {
            listener.onSubItemClick(list)
        }


        holder.ivSelectMain.setOnClickListener {
            listener.onItemSelectClick(list,position)
        }



        if(list.is_selected!!){
            holder.ivSelectMain.setImageResource(R.drawable.tickmark)
        }else
            holder.ivSelectMain.setImageResource(R.drawable.untickmark)

    }

    class ViewHolder(itemView :View):RecyclerView.ViewHolder(itemView) {
        val tvNumberMain = itemView.findViewById(R.id.tvNumberMainInv) as AppCompatTextView
        val tvItemMain = itemView.findViewById(R.id.tvItemMainInv) as AppCompatTextView
        val tvRemarksMain = itemView.findViewById(R.id.tvRemarksMainInv) as AppCompatTextView
        val tvSubStockCount = itemView.findViewById(R.id.tvSubStockCountInv) as AppCompatTextView
        val llHeaderMain = itemView.findViewById(R.id.llHeaderMainInv) as LinearLayout
        val llMainListCount = itemView.findViewById(R.id.llMainListCountInv) as LinearLayout
        val ivSelectMain = itemView.findViewById(R.id.ivSelectMainInv) as ImageView
    }


}