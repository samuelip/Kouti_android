package com.agile.kouti.quotation.subItem.quotation

import com.agile.kouti.db.quotation.MainItemQuotation
import com.agile.kouti.db.quotation.SubItemQuotation

interface MainItemClickListener {
    fun onMainItemClick(list: MainItemQuotation)

    fun onItemSelectClick(list: MainItemQuotation, pos: Int)

    fun onSubItemClick(list: MainItemQuotation)

}