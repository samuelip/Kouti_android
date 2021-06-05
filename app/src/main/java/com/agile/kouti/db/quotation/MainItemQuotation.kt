package com.agile.kouti.db.quotation

data class MainItemQuotation (
    val id:String?="",
    val item:String?="",
    val description:String?="",
    val project:String?="",
    val amount:String?="",
    val discount:String?="",
    val net_amount:String?="",
    val stock_list:String?="",
    val total_sub_item:String?="",
    var is_selected: Boolean?=false
)