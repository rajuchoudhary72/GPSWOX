package com.shazcom.gps.app.data.response

import com.google.gson.annotations.SerializedName

data class SystemEventData(
    val id: Int,
    val value: String,
    val title: String,
    var isChecked: Boolean = false
) {
    override fun toString(): String {
        return title.replace("(teltonika)", "")
    }
}

data class SystemEvents(
    val key: String,
    val name: String,
    val items: List<SystemEventData>
)

data class GetCustomEventResponse(
    @SerializedName("0") val systemEvents: SystemEvents
)