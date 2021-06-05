package com.agile.kouti.db

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChartOfAccount (
    val id:String?="",
    val user_id: String?="",
    var account_nature: String?="",
    var second_level: String?="",
    var third_level: String?="",
    val account_no: String?="",
    val account_name: String?="",
    val color: String?="",
    var is_selected: Boolean?=false,
    var created_date: String?=""
): Parcelable

