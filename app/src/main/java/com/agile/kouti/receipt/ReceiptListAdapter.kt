package com.agile.kouti.receipt

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
import com.agile.kouti.db.receipt.Receipt
import com.agile.kouti.invoice.invoice_list.InvoiceListAdapter

class ReceiptListAdapter(private val mContext: Context,
                         private var receiptList: ArrayList<Receipt>,
                         private val listener: ReceiptClickListener): RecyclerView.Adapter<ReceiptListAdapter.ViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.receipt_item_row, parent, false)
        return ReceiptListAdapter.ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var list = receiptList[position]

        holder.tvNumber.text = (position+1).toString()
        holder.tvDate.text = list.date
        holder.tvReceiptNo.text = list.receipt_number

        holder.llRow.setOnClickListener {
            listener.onReceiptItemClick(list)
        }

        holder.ivSelectRec.setOnClickListener {
            listener.onItemSelectClick(list,position)
        }

        if(list.is_selected!!){
            holder.ivSelectRec.setImageResource(R.drawable.tickmark)
        }else
            holder.ivSelectRec.setImageResource(R.drawable.untickmark)
    }

    override fun getItemCount(): Int {
        return receiptList.size
    }

    fun filterList(filteredList: ArrayList<Receipt>) {
        this.receiptList = filteredList
        notifyDataSetChanged();
    }

    class ViewHolder (itemView : View):RecyclerView.ViewHolder(itemView){
        val tvNumber = itemView.findViewById(R.id.tvNumberRec) as AppCompatTextView
        val tvDate = itemView.findViewById(R.id.tvDateRec) as AppCompatTextView
        val tvReceiptNo = itemView.findViewById(R.id.tvReceiptNo) as AppCompatTextView
        val ivSelectRec = itemView.findViewById(R.id.ivSelectRec) as ImageView
        val llRow = itemView.findViewById(R.id.llRowRec) as LinearLayout
    }
}