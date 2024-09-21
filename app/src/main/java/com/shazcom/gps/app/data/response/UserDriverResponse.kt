package com.shazcom.gps.app.data.response

data class DriverData(
    val id: Int,
    val user_id: Int,
    val device_id: Int,
    val name: String,
    val rfid: String,
    val phone: String,
    val email: String,
    val description: String
)

data class Drivers(
    val data: List<DriverData>
)

data class DriverResult(
    val drivers: Drivers
)

data class UserDriverResponse(
    val status: Int,
    val items: DriverResult
)