package com.agile.kouti.sale

import com.agile.kouti.home.SetupModel

interface OnItemClickListener {
    fun onItemClick(list: SalesModel,pos:Int)
}