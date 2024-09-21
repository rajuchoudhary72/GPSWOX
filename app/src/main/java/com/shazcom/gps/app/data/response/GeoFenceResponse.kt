package com.shazcom.gps.app.data.response

data class GeoFenceData(
    val id: Int,
    val user_id: Int,
    val group_id: Int,
    val active: Int,
    val name: String,
    val coordinates: String,
    val polygon_color: String,
    val created_at: String,
    val updated_at: String,
    val type: String
)

data class GeoFenceResult(
    val geofences: List<GeoFenceData>
)

data class GeoFenceResponse(
    val status: Int,
    val items: GeoFenceResult
)