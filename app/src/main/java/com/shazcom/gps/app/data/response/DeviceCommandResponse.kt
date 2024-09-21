package com.shazcom.gps.app.data.response

data class DeviceCommandData(
    val title: String,
    val name: String,
    val type: String,
    val description: String,
    val default: String
)

data class DeviceCommandResponse(
    val type: String,
    val title: String,
    val attributes: List<DeviceCommandData>
) {
    override fun toString(): String {
        return title
    }
}