package com.agile.kouti.db.payroll

import android.os.Parcel
import android.os.Parcelable

data class Staff (
    val id: String?="",
    val name: String?="",
    val group: String?=""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(group)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Staff> {
        override fun createFromParcel(parcel: Parcel): Staff {
            return Staff(parcel)
        }

        override fun newArray(size: Int): Array<Staff?> {
            return arrayOfNulls(size)
        }
    }
}