package com.shazcom.gps.app.data.response

data class BaseResponse(
    val status: Int,
    val url : String,
    val error: Any
)