package com.agile.kouti.payroll

import com.agile.kouti.db.payroll.PayRoll
import com.agile.kouti.db.payroll.PayRollItem

interface PayrollItemClickListener {

    fun onPayrollItemClick(list: PayRollItem)
}