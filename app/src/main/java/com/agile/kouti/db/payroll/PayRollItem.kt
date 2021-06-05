package com.agile.kouti.db.payroll

import android.os.Parcel
import android.os.Parcelable

data class PayRollItem(
    val amount: String?="",
    val id: String? ="",
    var item: String?="",
    val remark: String?="",
    var shop: String?="",
    var vendor: String? =""


):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(amount)
        parcel.writeString(id)
        parcel.writeString(item)
        parcel.writeString(remark)
        parcel.writeString(shop)
        parcel.writeString(vendor)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PayRollItem> {
        override fun createFromParcel(parcel: Parcel): PayRollItem {
            return PayRollItem(parcel)
        }

        override fun newArray(size: Int): Array<PayRollItem?> {
            return arrayOfNulls(size)
        }
    }
}