package com.shazcom.gps.app.data.response

import com.google.gson.annotations.SerializedName

data class Request_method_select(
    @SerializedName("id") val id: String,
    @SerializedName("value") val value: String,
    @SerializedName("title") val title: String
)

data class Groups(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val user_id: Int,
    @SerializedName("title") val title: String
) {
    override fun toString(): String {
        return "{\"id \": $id, \"title \": $title}"
    }
}

data class Timezones(
    @SerializedName("id") val id: Int,
    @SerializedName("value") val value: String,
    @SerializedName("title") val title: String
) {
    override fun toString(): String {
        return value
    }
}

data class Units_of_distance(
    @SerializedName("id") val id: String,
    @SerializedName("value") val value: String,
    @SerializedName("title") val title: String
) {
    override fun toString(): String {
        return title
    }
}

data class Units_of_altitude(
    @SerializedName("id") val id: String,
    @SerializedName("value") val value: String,
    @SerializedName("title") val title: String
) {
    override fun toString(): String {
        return title
    }
}

data class Units_of_capacity(
    @SerializedName("id") val id: String,
    @SerializedName("value") val value: String,
    @SerializedName("title") val title: String
) {
    override fun toString(): String {
        return title
    }
}


data class Dst_types(

    @SerializedName("id") val id: String,
    @SerializedName("value") val value: String,
    @SerializedName("title") val title: String
)

data class Dst_countries(

    @SerializedName("id") val id: Int,
    @SerializedName("value") val value: String,
    @SerializedName("title") val title: String
)

data class Authentication_select(

    @SerializedName("id") val id: Int,
    @SerializedName("value") val value: String,
    @SerializedName("title") val title: String
)

data class Weekdays(

    @SerializedName("id") val id: String,
    @SerializedName("value") val value: String,
    @SerializedName("title") val title: String
) {
    override fun toString(): String {
        return value
    }
}


data class Week_start_days(

    @SerializedName("id") val id: Int,
    @SerializedName("value") val value: String,
    @SerializedName("title") val title: String
)

data class Week_pos(

    @SerializedName("id") val id: String,
    @SerializedName("value") val value: String,
    @SerializedName("title") val title: String
)

data class Months(

    @SerializedName("id") val id: String,
    @SerializedName("value") val value: String,
    @SerializedName("title") val title: String
)

data class Encoding_select(

    @SerializedName("id") val id: String,
    @SerializedName("value") val value: String,
    @SerializedName("title") val title: String
)

data class SetUpItem(
    @SerializedName("timezone_id") val timezone_id: Int,
    @SerializedName("unit_of_altitude") val unit_of_altitude: String,
    @SerializedName("unit_of_distance") val unit_of_distance: String,
    @SerializedName("unit_of_capacity") val unit_of_capacity: String
)

data class EditUserDataResponse(
    @SerializedName("item") val item: SetUpItem,
    @SerializedName("timezones") val timezones: List<Timezones>,
    @SerializedName("units_of_distance") val units_of_distance: List<Units_of_distance>,
    @SerializedName("units_of_capacity") val units_of_capacity: List<Units_of_capacity>,
    @SerializedName("units_of_altitude") val units_of_altitude: List<Units_of_altitude>,
    @SerializedName("groups") val groups: List<Groups>,
    @SerializedName("sms_queue_count") val sms_queue_count: Int,
    @SerializedName("sms_gateway") val sms_gateway: Int,
    @SerializedName("request_method_select") val request_method_select: List<Request_method_select>,
    @SerializedName("encoding_select") val encoding_select: List<Encoding_select>,
    @SerializedName("authentication_select") val authentication_select: List<Authentication_select>,
    @SerializedName("dst_types") val dst_types: List<Dst_types>,
    //@SerializedName("user_dst") val user_dst: String,
    @SerializedName("months") val months: List<Months>,
    @SerializedName("weekdays") val weekdays: List<Weekdays>,
    @SerializedName("week_pos") val week_pos: List<Week_pos>,
    @SerializedName("dst_countries") val dst_countries: List<Dst_countries>,
    @SerializedName("week_start_days") val week_start_days: List<Week_start_days>,
    @SerializedName("status") val status: Int
)