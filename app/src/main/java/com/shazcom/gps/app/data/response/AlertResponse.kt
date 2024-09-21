package com.shazcom.gps.app.data.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NotificationValue(
    val active: String?,
    val input: String?
) : Parcelable

@Parcelize
data class AlertNotification(
    val sound: NotificationValue?,
    val push: NotificationValue?,
    val email: NotificationValue?
) : Parcelable

@Parcelize
data class AlertData(
    val id: Int?,
    val user_id: Int?,
    val active: Int?,
    val name: String?,
    val type: String?,
    val devices: List<Int>,
    val events_custom: List<Int>,
    val geofences: List<Int>,
    val drivers: List<Int>,
    val notifications: AlertNotification?
) : Parcelable

data class AlertList(val alerts: List<AlertData>)

data class AlertResponse(val status: Int, val items: AlertList)