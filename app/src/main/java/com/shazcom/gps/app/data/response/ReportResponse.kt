package com.shazcom.gps.app.data.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ReportData(
    val id: Int,
    val title: String,
    val type: Int,
    val format: String,
    val daily: String,
    val daily_time: String,
    val weekly: String,
    val weekly_time: String,
    val email: String,
    val monthly: String,
    val monthly_time: String,
    val devices: List<Int>
) : Parcelable

data class ReportItem(
    val total: Int,
    val per_page: Int,
    val last_page: Int,
    val data : List<ReportData>
)

data class ReportList(
  val reports: ReportItem
)

data class ReportResponse(
    val status : Int,
    val items: ReportList
)
