package com.agile.kouti.manage_company

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.agile.kouti.R
import com.agile.kouti.db.User
import com.agile.kouti.db.invoice.Invoice
import com.agile.kouti.invoice.invoice_list.InvoiceListAdapter

class UserListAdapter(private val mContext: Context,
                      private var userList: ArrayList<User>,
                      private val listener: UserClickListener): RecyclerView.Adapter<UserListAdapter.ViewHolder>()  {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_user_list, parent, false)
        return UserListAdapter.ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var list = userList[position]

        holder.tvName.text = list.name
        holder.tvPhoneNo.text = list.phone

        holder.llRowUser.setOnClickListener {
            listener.onUserClick(list)
        }
    }

    fun filterList(filteredList: ArrayList<User>) {
        this.userList = filteredList
        notifyDataSetChanged();
    }

    class ViewHolder  (itemView : View):RecyclerView.ViewHolder(itemView){
        val tvName = itemView.findViewById(R.id.tvName) as AppCompatTextView
        val tvPhoneNo = itemView.findViewById(R.id.tvPhoneNo) as AppCompatTextView
        val llRowUser = itemView.findViewById(R.id.llRowUser) as LinearLayout
    }


}