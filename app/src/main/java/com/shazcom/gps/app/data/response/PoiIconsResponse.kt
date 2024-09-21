package com.shazcom.gps.app.data.response

data class IconItems(
    val id: Int,
    val path: String,
    val url: String
)

data class PoiIconsResponse(
    val status: Int,
    val items: List<IconItems>
)