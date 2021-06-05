package com.agile.kouti.quotation.subItem.quotation

import com.agile.kouti.db.quotation.Quotation

interface QuotationClickListener {
    fun onQuotationItemClick(list: Quotation)

    fun onItemSelectClick(list: Quotation, pos: Int)

    fun onMainItemSelectClick(list: Quotation, pos: Int)
}