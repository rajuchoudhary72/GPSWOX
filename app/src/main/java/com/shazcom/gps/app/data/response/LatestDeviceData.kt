package com.shazcom.gps.app.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


@Parcelize
data class LatestItem(
    @SerializedName("id") val id: Int,
    @SerializedName("alarm") val alarm: Int,
    @SerializedName("name") val name: String,
    @SerializedName("online") val online: String,
    @SerializedName("time") val time: String,
    @SerializedName("timestamp") val timestamp: Int,
    @SerializedName("acktimestamp") val acktimestamp: Int,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
    @SerializedName("course") val course: Int,
    @SerializedName("speed") val speed: Int,
    @SerializedName("altitude") val altitude: Int,
    @SerializedName("icon_type") val icon_type: String,
    @SerializedName("icon_color") val icon_color: String,
    @SerializedName("icon_colors") val icon_colors: Icon_colors,
    @SerializedName("icon") val icon: Icon,
    @SerializedName("power") val power: String,
    @SerializedName("address") val address: String,
    @SerializedName("protocol") val protocol: String,
    @SerializedName("driver") val driver: String,
    @SerializedName("driver_data") val driver_data: Driver_data,
    @SerializedName("sensors") val sensors: String,
    @SerializedName("services") val services: String,
    @SerializedName("tail") val tail: String,
    @SerializedName("distance_unit_hour") val distance_unit_hour: String,
    @SerializedName("unit_of_distance") val unit_of_distance: String,
    @SerializedName("unit_of_altitude") val unit_of_altitude: String,
    @SerializedName("unit_of_capacity") val unit_of_capacity: String,
    @SerializedName("stop_duration") val stop_duration: String,
    @SerializedName("moved_timestamp") val moved_timestamp: Int,
    @SerializedName("engine_status") val engine_status: Boolean,
    @SerializedName("detect_engine") val detect_engine: String,
    @SerializedName("engine_hours") val engine_hours: String,
    @SerializedName("total_distance") val total_distance: Double

) : Parcelable {
    override fun toString(): String {
        return name
    }
}


@Parcelize
data class LatestDeviceData(
    var items: List<LatestItem>,
    var time : Long
) : Parcelable