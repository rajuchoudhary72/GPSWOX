package com.shazcom.gps.app.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Pivot(

    val user_id: Int?,
    val device_id: Int?,
    val group_id: Int?,
    val current_driver_id: String?,
    val active: Int?,
    val timezone_id: String?
) : Parcelable

@Parcelize
data class Sensors(

    val id: Int?,
    val user_id: Int?,
    val device_id: Int?,
    val name: String?,
    val type: String?,
    val tag_name: String?,
    val add_to_history: Int?,
    val on_value: String?,
    val off_value: String?,
    val shown_value_by: String?,
    val fuel_tank_name: String?,
    val full_tank: String?,
    val full_tank_value: String?,
    val min_value: Int?,
    val max_value: Int?,
    val formula: String?,
    val odometer_value_by: String?,
    val odometer_value: String?,
    val odometer_value_unit: String?,
    val temperature_max: String?,
    val temperature_max_value: String?,
    val temperature_min: String?,
    val temperature_min_value: String?,
    val value: String?,
    val value_formula: Int?,
    val show_in_popup: Int?,
    val unit_of_measurement: String?,
    val on_tag_value: String?,
    val off_tag_value: String?,
    val on_type: String?,
    val off_type: String?,
    val calibrations: String?,
    val skip_calibration: String?
) : Parcelable

@Parcelize
data class Tail(
    val lat: Double?,
    val lng: Double?
) : Parcelable

@Parcelize
data class Traccar(

    val id: Int?,
    val name: String?,
    val uniqueId: Long?,
    val latestPosition_id: Int?,
    val lastValidLatitude: Double?,
    val lastValidLongitude: Double?,
    val other: String?,
    val speed: Double?,
    val time: String?,
    val device_time: String?,
    val server_time: String?,
    val ack_time: String?,
    val altitude: Int?,
    val course: Int?,
    val power: String?,
    val address: String?,
    val protocol: String?,
    val latest_positions: String?,
    val moved_at: String?,
    val stoped_at: String?,
    val engine_on_at: String?,
    val engine_off_at: String?
) : Parcelable

@Parcelize
data class Users(
    val id: Int?,
    val email: String?
) : Parcelable

@Parcelize
data class Services(
    val id: Int?,
    val name: String?,
    val value: String?,
    val expiring: Boolean,
    var deviceName: String?,
    var expiration_by: String?,
    var interval: Int?,
    var last_service: String?,
    var expires_date: String?
) : Parcelable

@Parcelize
data class Device_data(

    val services: List<Services>?
//    val id: Int?,
//    val user_id: Int?,
//    val current_driver_id: String?,
//    val timezone_id: String?,
//    val traccar_device_id: Int?,
//    val icon_id: Int?,
//    val icon_colors: Icon_colors,
//    val active: Int?,
//    val deleted: Int?,
//    val name: String?,
//    val imei: Long?,
//    val fuel_measurement_id: Int?,
//    val fuel_quantity: Double?,
//    val fuel_price: Double?,
//    val fuel_per_km: Double?,
//    val sim_number: String?,
//    val device_model: String?,
//    val plate_number: String?,
//    val vin: String?,
//    val registration_number: String?,
//    val object_owner: String?,
//    val additional_notes: String?,
//    val expiration_date: String?,
//    val sim_expiration_date: String?,
//    val sim_activation_date: String?,
//    val installation_date: String?,
//    val tail_color: String?,
//    val tail_length: Int?,
//    val engine_hours: String?,
//    val detect_engine: String?,
//    val min_moving_speed: Int?,
//    val min_fuel_fillings: Int?,
//    val min_fuel_thefts: Int?,
//    val snap_to_road: Int?,
//    val gprs_templates_only: Int?,
//    val valid_by_avg_speed: Int?,
//    val parameters: String?,
//    val currents: String?,
//    val created_at: String?,
//    val updated_at: String?,
//    val forward: String?,
//    val stop_duration: String?,
//    val pivot: Pivot,
//    val traccar: Traccar,
//    val icon: Icon,
//    val sensors: List<Sensors>,
//    val services: List<Services>,
//    val driver: Driver_data,
//    val users: List<Users>,
//    val lastValidLatitude: Double?,
//    val lastValidLongitude: Double?,
//    val latest_positions: String?,
//    val icon_type: String?,
//    val group_id: Int?,
//    val user_timezone_id: String?,
//    val time: String?,
//    val course: Int?,
//    val speed: Int?
) : Parcelable

@Parcelize
data class Driver_data(

    val id: String?,
    val user_id: String?,
    val device_id: String?,
    val name: String?,
    val rfid: String?,
    val phone: String?,
    val email: String?,
    val description: String?,
    val created_at: String?,
    val updated_at: String?
) : Parcelable

@Parcelize
data class Icon(

    val id: Int?,
    val user_id: String?,
    val type: String?,
    val order: Int?,
    val width: Int?,
    val height: Int?,
    val path: String?,
    val by_status: Int?
) : Parcelable

@Parcelize
data class Icon_colors(

    val moving: String?,
    val stopped: String?,
    val offline: String?,
    val engine: String?
) : Parcelable

@Parcelize
data class SensorMain(

    val id: Int?,
    val type: String?,
    val name: String?,
    val value: String?,
    @SerializedName("val")
    val values: String?
) : Parcelable

@Parcelize
data class Items(
    var isChecked: Boolean? = false,
    val id: Int?,
    val alarm: Int?,
    val name: String?,
    val online: String?,
    val time: String?,
    val timestamp: Long?,
    val acktimestamp: Long?,
    val lat: Double?,
    val lng: Double?,
    val course: Int?,
    val speed: Int?,
    val altitude: Int?,
    val icon_type: String?,
    val icon_color: String?,
    //val icon_colors: Icon_colors,
    val icon: Icon,
    val power: String?,
    val address: String?,
    val protocol: String?,
    val driver: String?,
    val driver_data: Driver_data,
    val sensors: List<SensorMain>?,
    val services: List<Services>?,
    val tail: List<Tail>?,
    val distance_unit_hour: String?,
    val unit_of_distance: String?,
    val unit_of_altitude: String?,
    val unit_of_capacity: String?,
    val stop_duration: String?,
    val moved_timestamp: Long?,
    val engine_status: Boolean,
    val detect_engine: String?,
    val engine_hours: String?,
    val total_distance: Double?,
    val device_data: Device_data
) : Parcelable {
    override fun toString(): String {
        return name!!
    }
}

@Parcelize
data class DeviceData(
    val isChecked: Boolean? = false,
    val id: Int?,
    val title: String?,
    var items: List<Items>?
) : Parcelable
