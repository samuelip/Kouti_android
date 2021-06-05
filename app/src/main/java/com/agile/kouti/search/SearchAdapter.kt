package com.agile.kouti.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.agile.kouti.R
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class SearchAdapter(
    private val context: Context,
    private var searchList: ArrayList<SearchModel>,
    private val listener: OnSearchClickListener):RecyclerView.Adapter<SearchAdapter.ViewHolder>(){

//    var searchFilterList = ArrayList<SearchModel>()
//    init {
//        searchFilterList = searchList
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.search_item,parent,false)
        return SearchAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return searchList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = searchList[position]
        holder.tvTitle.text = list.name

        holder.llRow.setOnClickListener {
            listener.onSearchItemClick(list)
        }
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val tvTitle = itemView.findViewById(R.id.tvTitle) as AppCompatTextView
        val llRow = itemView.findViewById(R.id.llRow) as LinearLayout
    }

    fun filterList(filteredCourseList: ArrayList<SearchModel>) {
        this.searchList = filteredCourseList
        notifyDataSetChanged();
    }


}