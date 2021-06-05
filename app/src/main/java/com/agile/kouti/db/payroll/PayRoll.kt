package com.agile.kouti.db.payroll

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


data class PayRoll(
    val currency: String? = "",
    val date: String? = "",
    val description: String? = "",
    val expense_no: String? = "",
    val id: String? = "",
    var is_selected: Boolean = false,
    val staff_list: String? = "",
    val total_amount: String? = "",
    val upload_document: String? = "",
    val user_id: String? = "",
    val created_date: String? = "",
    val color: String? = ""


) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(currency)
        parcel.writeString(date)
        parcel.writeString(description)
        parcel.writeString(expense_no)
        parcel.writeString(id)
        parcel.writeByte(if (is_selected) 1 else 0)
        parcel.writeString(staff_list)
        parcel.writeString(total_amount)
        parcel.writeString(upload_document)
        parcel.writeString(user_id)
        parcel.writeString(created_date)
        parcel.writeString(color)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PayRoll> {
        override fun createFromParcel(parcel: Parcel): PayRoll {
            return PayRoll(parcel)
        }

        override fun newArray(size: Int): Array<PayRoll?> {
            return arrayOfNulls(size)
        }
    }
}
