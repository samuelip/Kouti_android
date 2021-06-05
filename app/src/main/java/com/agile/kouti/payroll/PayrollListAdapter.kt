package com.agile.kouti.payroll

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.agile.kouti.R
import com.agile.kouti.db.payroll.PayRoll

class PayrollListAdapter(private val mContext: Context,
                         private var payrollList: ArrayList<PayRoll>,
                         private val listener: PayrollClickListener): RecyclerView.Adapter<PayrollListAdapter.ViewHolder>()  {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.payroll_expense_item, parent, false)
        return PayrollListAdapter.ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return payrollList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = payrollList[position]

        holder.tvNumber.text = (position+1).toString()
        holder.tvDate.text = list.date
        holder.tvExpenseNo.text = list.expense_no
        holder.cvPayroll.setCardBackgroundColor(Color.parseColor(list.color!!))

        holder.llRow.setOnClickListener {
            listener.onPayrollItemClick(list)
        }

        holder.ivSelect.setOnClickListener {
            listener.onItemSelectClick(list,position)
        }

        if(list.is_selected!!){
            holder.ivSelect.setImageResource(R.drawable.tickmark)
        }else
            holder.ivSelect.setImageResource(R.drawable.untickmark)

    }

    fun filterList(filteredCourseList: ArrayList<PayRoll>) {
        this.payrollList = filteredCourseList
        notifyDataSetChanged();

    }




    class ViewHolder (itemView : View):RecyclerView.ViewHolder(itemView){
        val tvNumber = itemView.findViewById(R.id.tvNumber) as AppCompatTextView
        val tvDate = itemView.findViewById(R.id.tvDate) as AppCompatTextView
        val tvExpenseNo = itemView.findViewById(R.id.tvExpenseNo) as AppCompatTextView
        val ivSelect = itemView.findViewById(R.id.ivSelect) as ImageView
        val llRow = itemView.findViewById(R.id.llRow) as LinearLayout
        val cvPayroll = itemView.findViewById(R.id.cv_payroll) as CardView


    }


}