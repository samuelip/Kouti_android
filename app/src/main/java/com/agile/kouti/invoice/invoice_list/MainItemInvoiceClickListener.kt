package com.agile.kouti.invoice.invoice_list

import com.agile.kouti.db.invoice.MainItemInvoice
import com.agile.kouti.db.quotation.MainItemQuotation

interface MainItemInvoiceClickListener {
    fun onMainItemClick(list: MainItemInvoice)

    fun onItemSelectClick(list: MainItemInvoice, pos: Int)

    fun onSubItemClick(list: MainItemInvoice)
}