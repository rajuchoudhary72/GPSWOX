package com.shazcom.gps.app.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EventData(

    @SerializedName("id") val id: Int?,
    @SerializedName("user_id") val user_id: Int?,
    @SerializedName("device_id") val device_id: Int?,
    @SerializedName("geofence_id") val geofence_id: String?,
    @SerializedName("position_id") val position_id: Int?,
    @SerializedName("alert_id") val alert_id: Int?,
    @SerializedName("type") val type: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("altitude") val altitude: Int?,
    @SerializedName("course") val course: String?,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("power") val power: String?,
    @SerializedName("speed") val speed: Int?,
    @SerializedName("time") val time: String?,
    @SerializedName("deleted") val deleted: Int?,
    @SerializedName("created_at") val created_at: String?,
    @SerializedName("updated_at") val updated_at: String?,
    //@SerializedName("additional") val additional: Additional,
    @SerializedName("device_name") val device_name: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("detail") val detail: String?
    //@SerializedName("geofence") val geofence: String?
) : Parcelable

@Parcelize
data class Additional(
    @SerializedName("geofence") val geofence: String?
): Parcelable

@Parcelize
data class EventItems(

    @SerializedName("url") val url: String?,
    @SerializedName("total") val total: Int?,
    @SerializedName("per_page") val per_page: Int?,
    @SerializedName("current_page") val current_page: Int?,
    @SerializedName("last_page") val last_page: Int?,
    @SerializedName("next_page_url") val next_page_url: String?,
    @SerializedName("prev_page_url") val prev_page_url: String?,
    @SerializedName("from") val from: Int?,
    @SerializedName("to") val to: Int?,
    @SerializedName("data") val data: List<EventData>
) : Parcelable

@Parcelize
data class EventResponse(
    @SerializedName("status") val status: Int?,
    @SerializedName("items") val items: EventItems
) : Parcelable