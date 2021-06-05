package com.agile.kouti.settings

import com.agile.kouti.db.invoice.Invoice

interface MenuCheckListener {

    fun onItemCheckChange( pos: Int,isChecked : Boolean)

}