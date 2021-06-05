package com.agile.kouti.crm

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.agile.kouti.R
import com.agile.kouti.db.crm.Person
import com.agile.kouti.payroll.AddItemPayrollAdapter
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_add_pay_roll.*

class PersonAdapter(
    private val context: Context,
    private var personList: ArrayList<Person>,
    private val listener: PersonItemClickListener):RecyclerView.Adapter<PersonAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_person_row, parent, false)
        return PersonAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return  personList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = personList[position]

        holder.tvName.text = list.name
        holder.tvRemark.text = list.remark
        holder.tvBirthday.text = list.birthday

        Glide.with(holder.ivProfilePic)
            .load(list.profile_pic)
            .centerCrop()
            .placeholder(R.drawable.upload_document)
            .into(holder.ivProfilePic)

        holder.llHeaderRow.setOnClickListener {
            listener.onPersonItemClick(list)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName = itemView.findViewById(R.id.tvName) as AppCompatTextView
        val tvRemark = itemView.findViewById(R.id.tvRemark) as AppCompatTextView
        val tvBirthday = itemView.findViewById(R.id.tvBirthday) as AppCompatTextView
        val ivProfilePic = itemView.findViewById(R.id.ivProfilePic) as AppCompatImageView
        val llHeaderRow = itemView.findViewById(R.id.llHeaderRow) as LinearLayout
    }


}