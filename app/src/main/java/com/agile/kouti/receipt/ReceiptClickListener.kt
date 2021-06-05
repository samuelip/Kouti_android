package com.agile.kouti.receipt

import com.agile.kouti.db.receipt.Receipt

interface ReceiptClickListener {

    fun onReceiptItemClick(list: Receipt)

    fun onItemSelectClick(list: Receipt, pos: Int)

}