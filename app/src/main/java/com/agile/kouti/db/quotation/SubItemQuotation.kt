package com.agile.kouti.db.quotation


/****
 *
 * This Table common for invoice sub item & Quotation sub item *
 *
 ***/
data class SubItemQuotation (
    val id:String?="",
    var stock_name:String?="",
    var location:String?="",
    val shop:String?="",
    val specification:String?="",
    val description:String?="",
    val quntity:String?="",
    val unit:String?="",
    val up:String?="",
    val discount:String?="",
    val np:String?="",
    val line_total:String?="",
    val stock_date:String?="",
    var is_selected: Boolean?=false
)