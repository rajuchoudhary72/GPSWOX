package com.shazcom.gps.app.data.response

data class Protocols(
    val id: String,
    val value: String,
    val title: String
) {
    override fun toString(): String {
        return title
    }
}

data class Types(
    val id: Int,
    val value: String,
    val title: String
){
    override fun toString(): String {
        return title
    }
}

data class ProtocolResponse(
    val status: Int,
    val protocols: List<Protocols>,
    val types: List<Types>
)