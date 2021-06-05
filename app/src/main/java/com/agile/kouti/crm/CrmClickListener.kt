package com.agile.kouti.crm

import com.agile.kouti.db.crm.Crm
import com.agile.kouti.db.payroll.PayRoll

interface CrmClickListener {
    fun onCrmItemClick(list: Crm)

    fun onItemSelectClick(list: Crm, pos: Int)
}