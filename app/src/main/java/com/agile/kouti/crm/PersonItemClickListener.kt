package com.agile.kouti.crm

import com.agile.kouti.db.crm.Person
import com.agile.kouti.db.payroll.PayRollItem

interface PersonItemClickListener {
    fun onPersonItemClick(list: Person)
}