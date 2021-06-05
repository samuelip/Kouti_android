package com.agile.kouti.receipt

import com.agile.kouti.db.receipt.ReceiptAccount

interface AccountItemClickListener {

    fun onAccountItemClick(list: ReceiptAccount)
}