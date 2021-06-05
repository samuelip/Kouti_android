package com.agile.kouti.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.agile.kouti.R
import com.agile.kouti.db.CurrencyTable
import kotlinx.android.synthetic.main.currency_dialog.*

class CurrencyDialog(
    var activity: Activity,
    private val list: ArrayList<CurrencyTable>,
    private val listener: CurrencyListener
) : DialogFragment(), CurrencyItemClickListener {


    private val currencyAdapter = CurrencyAdapter(list, this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.currency_dialog, container, false)
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)


    }

    override fun onResume() {
        super.onResume()
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        rvCurrency.layoutManager = layoutManager
        rvCurrency.adapter = currencyAdapter
    }



    override fun onCurrencyItemClick(list: CurrencyTable) {
        listener.onCurrencyClick(list)
        dialog?.dismiss()
    }

}