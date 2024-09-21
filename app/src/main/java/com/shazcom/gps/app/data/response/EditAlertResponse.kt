package com.shazcom.gps.app.data.response

data class EventCustom(
    val id: Int
)

data class EditItemData(
    val events_custom: List<EventCustom>
)

data class EditAlertResponse(
    val status: Int,
    val types: List<AlertTypes>,
    val item : EditItemData
)

