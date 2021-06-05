package com.agile.kouti.receipt

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.agile.kouti.R
import com.agile.kouti.db.receipt.ReceiptAccount
import com.agile.kouti.invoice.invoice_list.MainItemInvoiceAdapter

class ReceiptAccountAdapter(
    private val context: Context,
    private var accountList: ArrayList<ReceiptAccount>,
    private val listener: AccountItemClickListener) : RecyclerView.Adapter<ReceiptAccountAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_item_receipt_account, parent, false)
        return ReceiptAccountAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return  accountList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = accountList[position]

        holder.tvAccount.text = list.accounts_name
        holder.tvTotalAmount.text = list.total_amount

        holder.llRow.setOnClickListener {
            listener.onAccountItemClick(list)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAccount = itemView.findViewById(R.id.tvAccount) as AppCompatTextView
        val tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount) as AppCompatTextView
        val llRow = itemView.findViewById(R.id.llRowRec) as LinearLayout
    }

}