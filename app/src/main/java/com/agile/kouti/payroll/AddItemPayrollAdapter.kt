package com.agile.kouti.payroll

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.agile.kouti.R
import com.agile.kouti.db.AddItemPayRoll
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.payroll.Item
import com.agile.kouti.db.payroll.PayRollItem
import com.agile.kouti.search.SearchModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

class AddItemPayrollAdapter(
    private val context: Context,
    private var itemList: ArrayList<PayRollItem>,
    private val listener: PayrollItemClickListener
) : RecyclerView.Adapter<AddItemPayrollAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.add_payroll_item_row, parent, false)
        return AddItemPayrollAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = itemList[position]
        holder.tvItem.text = list.item
        holder.tvAmount.text = list.amount

        holder.llRow.setOnClickListener {
            listener.onPayrollItemClick(list)
        }

    }

    /* Get Value From Item Table*/



    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItem = itemView.findViewById(R.id.tvItem) as AppCompatTextView
        val tvAmount = itemView.findViewById(R.id.tvAmountData) as AppCompatTextView
        val llRow = itemView.findViewById(R.id.llRow) as LinearLayout
    }

}