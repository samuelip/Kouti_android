package com.agile.kouti.invoice.invoice_list

import com.agile.kouti.db.invoice.Invoice

interface InvoiceClickListener {

    fun onInvoiceItemClick(list: Invoice)

    fun onItemSelectClick(list: Invoice, pos: Int)

    fun onMainItemSelectClick(list: Invoice, pos: Int)
}