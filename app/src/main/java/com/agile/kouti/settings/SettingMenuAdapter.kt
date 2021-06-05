package com.agile.kouti.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.agile.kouti.R

class SettingMenuAdapter(
    private val context: Context,
    private var settingMenuList: ArrayList<MenuModel>,
    private var menuCheckListener: MenuCheckListener

) : RecyclerView.Adapter<SettingMenuAdapter.ViewHolder>() {


    internal  fun getList(): ArrayList<MenuModel> {
        return settingMenuList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_menu_item, parent, false)
        return SettingMenuAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return settingMenuList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = settingMenuList[position]

        holder.cbMenu.text = list.settingTitle

        holder.cbMenu.isChecked = list.isSelected

        holder.cbMenu.setOnCheckedChangeListener { buttonView, isChecked ->
            menuCheckListener.onItemCheckChange(position, isChecked)
        }


    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.cbMenu.setOnCheckedChangeListener(null)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cbMenu = itemView.findViewById(R.id.cb_menu) as CheckBox

    }


}