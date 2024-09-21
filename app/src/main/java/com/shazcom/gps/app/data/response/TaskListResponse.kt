package com.shazcom.gps.app.data.response


data class TaskData(
    val id: Int,
    val device_id: Int,
    val user_id: Int,
    val title: String,
    val comment: String,
    val priority: Int,
    val status: Int,
    val invoice_number: String,
    val pickup_address: String,
    val pickup_address_lat: Double?,
    val pickup_address_lng: Double?,
    val pickup_time_from: String,
    val pickup_time_to: String,
    val delivery_address: String,
    val delivery_address_lat: Double?,
    val delivery_address_lng: Double?,
    val delivery_time_from: String,
    val delivery_time_to: String
)

data class TaskItem(
    val total: Int,
    val per_page: Int,
    val current_page: Int,
    val last_page: Int,
    val data: List<TaskData>
)

data class TaskListResponse(
    val status: Int,
    val items: TaskItem
)
