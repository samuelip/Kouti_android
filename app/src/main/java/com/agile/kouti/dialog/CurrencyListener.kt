package com.agile.kouti.dialog

import com.agile.kouti.db.CurrencyTable

interface CurrencyListener {
    fun onCurrencyClick(list: CurrencyTable)
}