package com.agile.kouti.db.invoice

class Invoice (
    val id:String?="",
    val date:String?="",
    val status:String?="",
    val invoice_no:String?="",
    val credit_term_days:String?="",
    val customer:String?="",

    val correct_address:String?="",
    val delivery_address:String?="",
    val upload_document:String?="",
    val customer_po_no:String?="",
    val quotation_no:String?="",
    val debite_no:String?="",
    val delivery_note_no:String?="",
    val packing_list_no:String?="",
    val description1:String?="",
    val description2:String?="",
    val currency:String?="",
    val total:String?="",
    val item_list:String?="",
    val total_main_item:String?="",
    var is_selected: Boolean?=false,
    var created_date: String?="",
    var user_id: String?=""
)