package com.shazcom.gps.app.data.response

data class UserDataResponse(
    val email: String,
    val expiration_date: String,
    val days_left: String,
    val plan: String,
    val devices_limit: Int,
    val group_id: Int
)
