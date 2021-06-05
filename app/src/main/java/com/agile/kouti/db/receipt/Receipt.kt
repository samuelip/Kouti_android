package com.agile.kouti.db.receipt

data class Receipt (
    val id:String?="",
    val date:String?="",
    val receipt_number:String?="",
    val customer:String?="",
    val correct_address:String?="",
    val description:String?="",
    val currency:String?="",
    val local:String?="",
    val discount:String?="",
    val unsettled:String?="",
    val receipt_return:String?="",
    val receive:String?="",
    val total:String?="",
    val upload_document:String?="",
    val created_date:String?="",
    var is_selected: Boolean?=false,
    var user_id: String?="",
    val invoice_no:String?="",
    val invoice:String?="",
    val receipt_accountIds:String?=""

)