package com.shazcom.gps.app.data.response

data class LocalMapIcon(
    val id: Int,
    val path: String,
    val url: String
)

data class MapIcons(
    val id: Int,
    val user_id: Int,
    val map_icon_id: Int,
    val active: Int,
    val name: String,
    val description: String,
    val coordinates: String,
    val map_icon: LocalMapIcon
)

data class PoiItems(
    val mapIcons: List<MapIcons>
)

data class UserPoiResponse(
    val status: Int,
    val items: PoiItems
)