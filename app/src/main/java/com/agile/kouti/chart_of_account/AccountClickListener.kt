package com.agile.kouti.chart_of_account

import com.agile.kouti.db.ChartOfAccount
import com.agile.kouti.home.SetupModel

interface AccountClickListener {

    fun onAccountItemClick(list: ChartOfAccount)

    fun onItemSelectClick(list: ChartOfAccount,pos: Int)
}