package com.agile.kouti.db.quotation

data class Quotation (
    val id:String?="",
    val date:String?="",
    val status:String?="",
    val quotation_no:String?="",
    val customer:String?="",
    val correct_address:String?="",
    val delivery_address:String?="",
    val customer_po_no:String?="",
    val description1:String?="",
    val description2:String?="",
    val currency:String?="",
    val total:String?="",
    val upload_document:String?="",
    val item_list:String?="",
    val total_main_item:String?="",
    var is_selected: Boolean?=false,
    var created_date: String?="",
    var credit_term_days: String?="",
    var user_id: String?=""

)