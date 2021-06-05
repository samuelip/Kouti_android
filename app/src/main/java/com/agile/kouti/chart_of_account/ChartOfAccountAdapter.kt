package com.agile.kouti.chart_of_account

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
import com.agile.kouti.db.ChartOfAccount
import java.util.*

class ChartOfAccountAdapter(
    private val mContext: Context,
    private var accountList: ArrayList<ChartOfAccount>,
    private val listener: AccountClickListener
) : RecyclerView.Adapter<ChartOfAccountAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.chart_of_account_item, parent, false)
        return ChartOfAccountAdapter.ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return accountList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = accountList[position]

        holder.tvNumber.text = (position + 1).toString()
        holder.tvAccountNature.text = list.account_nature
        holder.tvSecondLevel.text = list.second_level
        // holder.tvAccountName.text = list.account_name

        holder.cvChartOfAccount.setCardBackgroundColor(Color.parseColor(list.color!!))

        holder.llRow.setOnClickListener {
            listener.onAccountItemClick(list)
        }

        holder.ivSelect.setOnClickListener {
            listener.onItemSelectClick(list, position)
        }

        if (list.is_selected!!) {
            holder.ivSelect.setImageResource(R.drawable.tickmark)
        } else
            holder.ivSelect.setImageResource(R.drawable.untickmark)

    }

    fun filterList(filteredCourseList: ArrayList<ChartOfAccount>) {
        this.accountList = filteredCourseList
        notifyDataSetChanged();
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNumber = itemView.findViewById(R.id.tvNumber) as AppCompatTextView
        val tvAccountNature = itemView.findViewById(R.id.tvAccountNature) as AppCompatTextView
        val tvSecondLevel = itemView.findViewById(R.id.tvSecondLevel) as AppCompatTextView

        //val tvAccountName = itemView.findViewById(R.id.tvAccountName) as AppCompatTextView
        val ivSelect = itemView.findViewById(R.id.ivSelect) as ImageView
        val llRow = itemView.findViewById(R.id.llRow) as LinearLayout
        val cvChartOfAccount = itemView.findViewById(R.id.cv_chart_of_account) as CardView
    }
}