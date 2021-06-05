package com.agile.kouti.db.crm

import android.os.Parcel
import android.os.Parcelable


data class Crm(
    val id: String? = "",
    val code: String? = "",
    val name: String? = "",
    val telephone: String? = "",
    val mobile: String? = "",
    val fax: String? = "",
    val email: String? = "",
    val keyword: String? = "",
    val coins: String? = "",
    val group: String? = "",
    val business_register_no: String? = "",
    val contact: String? = "",
    val gender: String? = "",
    val birthday: String? = "",
    val remark: String? = "",
    val join_date: String? = "",
    val membership_expiry_date: String? = "",
    val default_address: String? = "",
    val delivery_address: String? = "",
    val other_address: String? = "",
    val credit_limit: String? = "",
    val term_days: String? = "",
    val contact1: String? = "",
    val currency: String? = "",
    val return_account: String? = "",
    val referred_customer: String? = "",
    val height: String? = "",
    val weight: String? = "",
    val occupation: String? = "",
    val marital_status: String? = "",
    val person_detail: String? = "",
    val document_url: String? = "",
    val id_url: String? = "",
    var is_selected: Boolean? = false,
    var created_date: String? = "",
    var user_id: String? = "",
    var color: String? = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(code)
        parcel.writeString(name)
        parcel.writeString(telephone)
        parcel.writeString(mobile)
        parcel.writeString(fax)
        parcel.writeString(email)
        parcel.writeString(keyword)
        parcel.writeString(coins)
        parcel.writeString(group)
        parcel.writeString(business_register_no)
        parcel.writeString(contact)
        parcel.writeString(gender)
        parcel.writeString(birthday)
        parcel.writeString(remark)
        parcel.writeString(join_date)
        parcel.writeString(membership_expiry_date)
        parcel.writeString(default_address)
        parcel.writeString(delivery_address)
        parcel.writeString(other_address)
        parcel.writeString(credit_limit)
        parcel.writeString(term_days)
        parcel.writeString(contact1)
        parcel.writeString(currency)
        parcel.writeString(return_account)
        parcel.writeString(referred_customer)
        parcel.writeString(height)
        parcel.writeString(weight)
        parcel.writeString(occupation)
        parcel.writeString(marital_status)
        parcel.writeString(person_detail)
        parcel.writeString(document_url)
        parcel.writeString(id_url)
        parcel.writeValue(is_selected)
        parcel.writeString(created_date)
        parcel.writeString(user_id)
        parcel.writeString(color)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Crm> {
        override fun createFromParcel(parcel: Parcel): Crm {
            return Crm(parcel)
        }

        override fun newArray(size: Int): Array<Crm?> {
            return arrayOfNulls(size)
        }
    }
}
