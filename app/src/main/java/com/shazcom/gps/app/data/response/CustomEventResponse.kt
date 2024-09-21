package com.shazcom.gps.app.data.response

data class CustomEventData(
    val id: Int,
    val protocol: String,
    val message: String
)

data class CustomEvents(
    val data : List<CustomEventData>
)

data class CustomEventResult(
    val events:CustomEvents
)

data class CustomEventResponse(
    val status: Int,
    val items: CustomEventResult
)