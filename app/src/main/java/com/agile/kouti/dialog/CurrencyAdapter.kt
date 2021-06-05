package com.agile.kouti.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.agile.kouti.R
import com.agile.kouti.db.CurrencyTable

class CurrencyAdapter (
    private val currencyList: ArrayList<CurrencyTable>,
    internal var currencyItemClickListener: CurrencyItemClickListener): RecyclerView.Adapter<CurrencyAdapter.ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_currency_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var list = currencyList[position]
        holder.tvCurrency.text = list.currency

        holder.tvCurrency.setOnClickListener {
            currencyItemClickListener.onCurrencyItemClick(list)
        }
    }

    override fun getItemCount(): Int {
        return currencyList.size

    }


    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvCurrency = itemView.findViewById(R.id.tvCurrency) as AppCompatTextView
    }

}
