package com.agile.kouti.quotation.subItem.main_item

import com.agile.kouti.db.crm.Person
import com.agile.kouti.db.quotation.SubItemQuotation

interface SubItemClickListener {
    fun onSubItemClick(list: SubItemQuotation)

    fun onItemSelectClick(list: SubItemQuotation , pos:Int)
}