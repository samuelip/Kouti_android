package com.agile.kouti.db

data class User (
    val auth_id: String?="",
    val device_id: String?="",
    val device_type: String?="",
    val email: String?="",
    val fcm_token: String?="",
    val id: String?="",
    val name: String?="",
    val password: String?="",
    val phone: String?="",
    val created: String?=""
)