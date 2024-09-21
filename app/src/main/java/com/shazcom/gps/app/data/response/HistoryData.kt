package com.shazcom.gps.app.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ItemMain(
    @SerializedName("id") val id: Int??,
    @SerializedName("device_id") val device_id: Int?,
    @SerializedName("altitude") val altitude: Float?,
    @SerializedName("course") val course: Int?,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("other") val other: String?,
    @SerializedName("power") val power: String?,
    @SerializedName("speed") val speed: Int?,
    @SerializedName("time") val time: String?,
    @SerializedName("device_time") val device_time: String?,
    @SerializedName("server_time") val server_time: String?,
    @SerializedName("sensors_values") val sensors_values: String?,
    @SerializedName("valid") val valid: Int?,
    @SerializedName("distance") val distance: Double?,
    @SerializedName("protocol") val protocol: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("item_id") val item_id: String?,
    @SerializedName("raw_time") val raw_time: String?,
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lng") val lng: Double?,
    @SerializedName("other_arr") val other_arr: List<String>,
    @SerializedName("sensors_data") val sensors_data: List<Sensors_data>
) : Parcelable

@Parcelize
data class ItemsInner(
    @SerializedName("status") val status: Int?,
    @SerializedName("time") val time: String?,
    @SerializedName("show") val show: String?,
    @SerializedName("distance") val distance: Double?,
    @SerializedName("raw_time") val raw_time: String?,
    @SerializedName("items") val items: List<ItemMain>,
    @SerializedName("driver") val driver: Driver_data,
    @SerializedName("message") val message: String?,
    @SerializedName("fuel_consumption") val fuel_consumption: String?,
    @SerializedName("left") val left: String?,
    @SerializedName("top_speed") val topSpeed: Int?
) : Parcelable

@Parcelize
data class Item_class(

    @SerializedName("id") val id: Int?,
    @SerializedName("value") val value: String?,
    @SerializedName("title") val title: String?
) : Parcelable

@Parcelize
data class Images(

    @SerializedName("id") val id: Int?,
    @SerializedName("value") val value: String?,
    @SerializedName("title") val title: String?
) : Parcelable

@Parcelize
data class Icon_colorsInner(

    @SerializedName("moving") val moving: String?,
    @SerializedName("stopped") val stopped: String?,
    @SerializedName("offline") val offline: String?,
    @SerializedName("engine") val engine: String?
) : Parcelable

@Parcelize
data class DeviceInner(

    @SerializedName("id") val id: Int?,
    @SerializedName("user_id") val user_id: String?,
    @SerializedName("current_driver_id") val current_driver_id: String?,
    @SerializedName("timezone_id") val timezone_id: String?,
    @SerializedName("traccar_device_id") val traccar_device_id: Int?,
    @SerializedName("icon_id") val icon_id: Int?,
    @SerializedName("icon_colors") val icon_colors: Icon_colorsInner?,
    @SerializedName("active") val active: Int?,
    @SerializedName("deleted") val deleted: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("imei") val imei: String?,
    @SerializedName("fuel_measurement_id") val fuel_measurement_id: Int?,
    @SerializedName("fuel_quantity") val fuel_quantity: Double?,
    @SerializedName("fuel_price") val fuel_price: Double?,
    @SerializedName("fuel_per_km") val fuel_per_km: Double?,
    @SerializedName("sim_number") val sim_number: String?,
    @SerializedName("device_model") val device_model: String?,
    @SerializedName("plate_number") val plate_number: String?,
    @SerializedName("vin") val vin: String?,
    @SerializedName("registration_number") val registration_number: String?,
    @SerializedName("object_owner") val object_owner: String?,
    @SerializedName("additional_notes") val additional_notes: String?,
    @SerializedName("expiration_date") val expiration_date: String?,
    @SerializedName("sim_expiration_date") val sim_expiration_date: String?,
    @SerializedName("sim_activation_date") val sim_activation_date: String?,
    @SerializedName("installation_date") val installation_date: String?,
    @SerializedName("tail_color") val tail_color: String?,
    @SerializedName("tail_length") val tail_length: Int?,
    @SerializedName("engine_hours") val engine_hours: String?,
    @SerializedName("detect_engine") val detect_engine: String?,
    @SerializedName("min_moving_speed") val min_moving_speed: Int?,
    @SerializedName("min_fuel_fillings") val min_fuel_fillings: Int?,
    @SerializedName("min_fuel_thefts") val min_fuel_thefts: Int?,
    @SerializedName("snap_to_road") val snap_to_road: Int?,
    @SerializedName("gprs_templates_only") val gprs_templates_only: Int?,
    @SerializedName("valid_by_avg_speed") val valid_by_avg_speed: Int?,
    @SerializedName("parameters") val parameters: String?,
    @SerializedName("currents") val currents: String?,
    @SerializedName("created_at") val created_at: String?,
    @SerializedName("updated_at") val updated_at: String?,
    @SerializedName("forward") val forward: String?,
    @SerializedName("stop_duration") val stop_duration: String?,
    @SerializedName("sensors") val sensors: List<Sensors>,
    @SerializedName("traccar") val traccar: Traccar
) : Parcelable

@Parcelize
data class TraccarData(

    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("uniqueId") val uniqueId: Int?,
    @SerializedName("latestPosition_id") val latestPosition_id: Int?,
    @SerializedName("lastValidLatitude") val lastValidLatitude: Double?,
    @SerializedName("lastValidLongitude") val lastValidLongitude: Double?,
    @SerializedName("other") val other: String?,
    @SerializedName("speed") val speed: Double?,
    @SerializedName("time") val time: String?,
    @SerializedName("device_time") val device_time: String?,
    @SerializedName("server_time") val server_time: String?,
    @SerializedName("ack_time") val ack_time: String?,
    @SerializedName("altitude") val altitude: Float?,
    @SerializedName("course") val course: Int?,
    @SerializedName("power") val power: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("protocol") val protocol: String?,
    @SerializedName("latest_positions") val latest_positions: String?,
    @SerializedName("moved_at") val moved_at: String?,
    @SerializedName("stoped_at") val stoped_at: String?,
    @SerializedName("engine_on_at") val engine_on_at: String?,
    @SerializedName("engine_off_at") val engine_off_at: String?
) : Parcelable

@Parcelize
data class Sensors_data(

    @SerializedName("id") val id: String?,
    @SerializedName("value") val value: Double?
) : Parcelable

@Parcelize
data class SensorsData(

    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("sufix") val sufix: String?
) : Parcelable

@Parcelize
data class HistoryData(
    @SerializedName("items") val items: List<ItemsInner>,
    @SerializedName("distance_sum") val distance_sum: String?,
    @SerializedName("top_speed") val top_speed: String?,
    @SerializedName("move_duration") val move_duration: String?,
    @SerializedName("stop_duration") val stop_duration: String?,
    @SerializedName("fuel_consumption") val fuel_consumption: String?,
    //@SerializedName("device") val device: DeviceInner?,
    @SerializedName("sensors") val sensors: List<SensorsData>,
    @SerializedName("item_class") val item_class: List<Item_class>,
    @SerializedName("images") val images: List<Images>,
    @SerializedName("sensors_values") val sensors_values: List<String>,
    //@SerializedName("fuel_consumption_arr") val fuel_consumption_arr: List<String>,
    @SerializedName("status") val status: Int?
) : Parcelable