package com.agile.kouti.invoice.invoice_list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.agile.kouti.R
import com.agile.kouti.db.invoice.Invoice
import com.agile.kouti.db.quotation.Quotation
import com.agile.kouti.quotation.subItem.quotation.QuotationClickListener
import com.agile.kouti.quotation.subItem.quotation.QuotationListAdapter

class InvoiceListAdapter(private val mContext: Context,
                         private var invoiceList: ArrayList<Invoice>,
                         private val listener: InvoiceClickListener): RecyclerView.Adapter<InvoiceListAdapter.ViewHolder>()  {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.invoice_item_row, parent, false)
        return InvoiceListAdapter.ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return invoiceList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var list = invoiceList[position]

        holder.tvNumber.text = (position+1).toString()
        holder.tvDate.text = list.date
        holder.tvStatus.text = list.status
        holder.tvInvoiceNo.text = list.invoice_no
        holder.tvMainListCountInv.text = "("+list.total_main_item+") Main Item"

        holder.llRowInvoice.setOnClickListener {
            listener.onInvoiceItemClick(list)
        }

        holder.ivSelectInv.setOnClickListener {
            listener.onItemSelectClick(list,position)
        }

        holder.llMainListCountInv.setOnClickListener {
            listener.onMainItemSelectClick(list,position)
        }

        if(list.is_selected!!){
            holder.ivSelectInv.setImageResource(R.drawable.tickmark)
        }else
            holder.ivSelectInv.setImageResource(R.drawable.untickmark)
    }

    fun filterList(filteredList: ArrayList<Invoice>) {
        this.invoiceList = filteredList
        notifyDataSetChanged();
    }

    class ViewHolder (itemView : View):RecyclerView.ViewHolder(itemView){
        val tvNumber = itemView.findViewById(R.id.tvNumberInv) as AppCompatTextView
        val tvDate = itemView.findViewById(R.id.tvDateInv) as AppCompatTextView
        val tvStatus = itemView.findViewById(R.id.tvStatusInv) as AppCompatTextView
        val tvInvoiceNo = itemView.findViewById(R.id.tvInvoiceNo) as AppCompatTextView
        val tvMainListCountInv = itemView.findViewById(R.id.tvMainListCountInv) as AppCompatTextView
        val llRowInvoice = itemView.findViewById(R.id.llRowInvoice) as LinearLayout
        val llMainListCountInv = itemView.findViewById(R.id.llMainListCountInv) as LinearLayout
        val ivSelectInv = itemView.findViewById(R.id.ivSelectInv) as ImageView
    }
}