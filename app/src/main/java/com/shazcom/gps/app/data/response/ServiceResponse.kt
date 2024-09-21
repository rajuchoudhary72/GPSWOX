package com.shazcom.gps.app.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ServiceData(
    @SerializedName("id") val id: Int?,
    @SerializedName("user_id") val user_id: Int?,
    @SerializedName("device_id") val device_id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("expiration_by") val expiration_by: String?,
    @SerializedName("interval") val interval: Int?,
    @SerializedName("last_service") val last_service: String?,
    @SerializedName("trigger_event_left") val trigger_event_left: Int?,
    @SerializedName("renew_after_expiration") val renew_after_expiration: Int?,
    @SerializedName("expires") val expires: String?,
    @SerializedName("expires_date") val expires_date: String?,
    @SerializedName("remind") val remind: Int?,
    @SerializedName("remind_date") val remind_date: String?,
    @SerializedName("event_sent") val event_sent: Int?,
    @SerializedName("expired") val expired: Int?,
    @SerializedName("email") val email: String?,
    @SerializedName("mobile_phone") val mobile_phone: String?,
    @SerializedName("description") val description: String?
) : Parcelable

@Parcelize
data class ServiceResponse(
    @SerializedName("total") val total: Int?,
    @SerializedName("per_page") val per_page: Int?,
    @SerializedName("current_page") val current_page: Int?,
    @SerializedName("last_page") val last_page: Int?,
    @SerializedName("next_page_url") val next_page_url: String?,
    @SerializedName("prev_page_url") val prev_page_url: String?,
    @SerializedName("from") val from: Int?,
    @SerializedName("to") val to: Int?,
    @SerializedName("data") val data: List<ServiceData>,
    @SerializedName("url") val url: String?,
    @SerializedName("status") val status: Int?
) : Parcelable