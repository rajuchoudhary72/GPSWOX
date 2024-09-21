package com.shazcom.gps.app.data.response

import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import java.util.*

data class AlertOption(
    val id: Int,
    val title: String,
    var isChecked: Boolean = false
) {
    override fun toString(): String {
        return title
    }
}

data class AlertTypeAttribute(val name: String, val title: String, val type: String, val default: Any?, val options: List<AlertOption>)

data class AlertTypes(
    val type: String,
    val title: String,
    val attributes: List<AlertTypeAttribute>
) {
    override fun toString(): String {
        return title
    }
}

data class AlertDevices(
    val id: Int,
    val value: String,
    val title: String,
    var isChecked: Boolean = false
) {
    override fun toString(): String {
        return title
    }
}

data class AlertGeofence(
    val id: Int,
    val value: String,
    val title: String,
    var isChecked: Boolean = false
) {
    override fun toString(): String {
        return title
    }
}


data class AddAlertDataResponsee(
    val devices: List<AlertDevices>,
    val types: List<AlertTypes>,
    val geofences: List<AlertGeofence>
)