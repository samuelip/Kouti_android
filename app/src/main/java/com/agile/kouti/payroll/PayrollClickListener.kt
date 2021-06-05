package com.agile.kouti.payroll

import com.agile.kouti.db.payroll.PayRoll

interface PayrollClickListener {

    fun onPayrollItemClick(list: PayRoll)

    fun onItemSelectClick(list: PayRoll, pos: Int)
}