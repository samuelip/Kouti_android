package com.agile.kouti.crm

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
import com.agile.kouti.db.crm.Crm

class CrmListAdapter(private val mContext: Context,
                     private var crmList: ArrayList<Crm>,
                     private val listener: CrmClickListener): RecyclerView.Adapter<CrmListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.crm_item_row, parent, false)
        return CrmListAdapter.ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return crmList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var list = crmList[position]
        holder.tvNumber.text = (position+1).toString()
        holder.tvCode.text = list.code
        holder.tvCustomerName.text = list.name
        holder.cvCrm.setCardBackgroundColor(Color.parseColor(list.color!!))

        holder.llRow.setOnClickListener {
            listener.onCrmItemClick(crmList[position])
        }

        holder.ivSelect.setOnClickListener {
            listener.onItemSelectClick(list,position)
        }

        if(list.is_selected!!){
            holder.ivSelect.setImageResource(R.drawable.tickmark)
        }else
            holder.ivSelect.setImageResource(R.drawable.untickmark)

    }

    fun filterList(filteredCourseList: ArrayList<Crm>) {
        this.crmList = filteredCourseList
        notifyDataSetChanged();
    }


    class ViewHolder(itemView : View):RecyclerView.ViewHolder(itemView) {
        val tvCustomerName = itemView.findViewById(R.id.tvCustomerName) as AppCompatTextView
        val tvCode = itemView.findViewById(R.id.tvCode) as AppCompatTextView
        val tvNumber = itemView.findViewById(R.id.tvNumberCrm) as AppCompatTextView
        val ivSelect = itemView.findViewById(R.id.ivSelectCrm) as ImageView
        val llRow= itemView.findViewById(R.id.llRowCrm) as LinearLayout
        val cvCrm = itemView.findViewById(R.id.cv_crm) as CardView
    }


}