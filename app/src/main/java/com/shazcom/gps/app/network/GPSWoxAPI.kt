package com.shazcom.gps.app.network

import com.shazcom.gps.app.data.response.*
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface GPSWoxAPI {

    @Multipart
    @POST("login")
    suspend fun userAuth(
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody
    ): Response<AuthResponse>

    @Multipart
    @POST
    suspend fun forgotPassword(
        @Url url: String,
        @Part("email") email: RequestBody
    ): Response<BaseResponse>

    @GET("get_devices")
    suspend fun getDevices(
        @Query("lang") lang: String,
        @Query("user_api_hash") userHash: String
    ): Response<List<DeviceData>>

    @GET("get_devices_latest")
    suspend fun getDevicesLatest(
        @Query("lang") lang: String,
        @Query("user_api_hash") userHash: String
    ): Response<LatestDeviceData>

    @GET("get_history")
    suspend fun getHistory(
        @Query("lang") lang: String,
        @Query("user_api_hash") userHash: String,
        @Query("device_id") deviceId: Int,
        @Query("from_date") fromDate: String,
        @Query("from_time") fromTime: String,
        @Query("to_date") toDate: String,
        @Query("to_time") toTime: String

    ): Response<HistoryData>

    @GET("get_events")
    suspend fun getEvents(
        @Query("lang") lang: String,
        @Query("user_api_hash") userHash: String,
        @Query("device_id") deviceId: Int,
        @Query("page") page: Int
    ): Response<EventResponse>


    @GET("get_services")
    suspend fun getServices(
        @Query("lang") lang: String,
        @Query("user_api_hash") userHash: String,
        @Query("device_id") deviceId: Int,
        @Query("page") page: Int
    ): Response<ServiceResponse>

    @Multipart
    @POST("add_service")
    suspend fun addService(
        @Query("lang") lang: String,
        @Query("user_api_hash") userHash: String,
        @Query("device_id") deviceId: Int,
        @Part("device_id") device_id: RequestBody,
        @Part("name") name: RequestBody,
        @Part("expiration_by") expiration_by: RequestBody,
        @Part("interval") interval: RequestBody,
        @Part("last_service") last_service: RequestBody,
        @Part("trigger_event_left") trigger_event_left: RequestBody,
        @Part("renew_after_expiration") renew_after_expiration: RequestBody,
        @Part("email") email: RequestBody
    ): Response<BaseResponse>

    @Multipart
    @POST("edit_service")
    suspend fun editService(
        @Query("lang") lang: String,
        @Query("user_api_hash") userHash: String,
        @Part("id") id: RequestBody,
        @Part("device_id") device_id: RequestBody,
        @Part("name") name: RequestBody,
        @Part("expiration_by") expiration_by: RequestBody,
        @Part("interval") interval: RequestBody,
        @Part("last_service") last_service: RequestBody,
        @Part("trigger_event_left") trigger_event_left: RequestBody,
        @Part("renew_after_expiration") renew_after_expiration: RequestBody,
        @Part("email") email: RequestBody
    ): Response<BaseResponse>


    @GET("destroy_service")
    suspend fun deleteService(
        @Query("lang") lang: String,
        @Query("user_api_hash") userHash: String,
        @Query("service_id") serviceId: Int
    ): Response<BaseResponse>

    @Multipart
    @POST("generate_report")
    suspend fun generateReport(
        @Query("lang") lang: String,
        @Query("user_api_hash") userHash: String,
        @Part("type") type: RequestBody,
        @Part("format") format: RequestBody,
        @Part("devices[]") deviceArray: RequestBody,
        @Part("date_from") dateFrom: RequestBody,
        @Part("date_to") dateTo: RequestBody,
        @Part("send_to_email") email: RequestBody,
        @Part("show_addresses") showAddress: RequestBody,
        @Part("expense_type") expense_type: RequestBody,
        @Part("supplier") supplier: RequestBody,
        @Part("ignition_off") ignition_off: RequestBody
    ): Response<BaseResponse>

    @Multipart
    @POST("add_report")
    suspend fun addReport(
        @Query("lang") lang: String,
        @Query("user_api_hash") userHash: String,
        @Part("type") type: RequestBody,
        @Part("format") format: RequestBody,
        @Part("devices[]") deviceArray: RequestBody,
        @Part("date_from") dateFrom: RequestBody,
        @Part("date_to") dateTo: RequestBody,
        @Part("send_to_email") email: RequestBody,
        @Part("show_addresses") showAddress: RequestBody,
        @Part("daily") daily: RequestBody,
        @Part("expense_type") expense_type: RequestBody,
        @Part("supplier") supplier: RequestBody,
        @Part("ignition_off") ignition_off: RequestBody,
        @Part("weekly") weekly: RequestBody,
        @Part("monthly") monthly: RequestBody,
        @Part("daily_time") dailyTime: RequestBody,
        @Part("weekly_time") weeklyTime: RequestBody,
        @Part("monthly_time") monthlyTime: RequestBody,
        @Part("title") title: RequestBody
    ): Response<BaseResponse>


    @GET("edit_setup_data")
    suspend fun edtSetupData(
        @Query("lang") lang: String,
        @Query("user_api_hash") userHash: String
    ): Response<EditUserDataResponse>


    @GET("get_user_map_icons")
    suspend fun loadPOIMarkers(
        @Query("lang") lang: String?,
        @Query("user_api_hash") userHash: String?
    ): Response<UserPoiResponse>

    @GET("get_map_icons")
    suspend fun getPOIMapIcons(
        @Query("lang") lang: String,
        @Query("user_api_hash") user_api_hash: String
    ): Response<PoiIconsResponse>


    @POST("add_map_icon")
    suspend fun savePOIMarker(
        @Query("lang") lang: String?,
        @Query("user_api_hash") user_api_hash: String?,
        @Query("name") name: String?,
        @Query("description") description: String?,
        @Query("map_icon_id") map_icon_id: Int,
        @Query("coordinates") coordinates: String?
    ): Response<BaseResponse>


    @POST("edit_map_icon")
    suspend fun updatePOIMarker(
        @Query("lang") lang: String?,
        @Query("user_api_hash") user_api_hash: String?,
        @Query("id") id: Int?,
        @Query("name") name: String?,
        @Query("description") description: String?,
        @Query("map_icon_id") map_icon_id: Int,
        @Query("coordinates") coordinates: String?
    ): Response<BaseResponse>


    @GET("get_geofences")
    suspend fun loadGeoFence(
        @Query("lang") lang: String?,
        @Query("user_api_hash") userHash: String?
    ): Response<GeoFenceResponse>


    @Multipart
    @POST("add_geofence")
    suspend fun addNewGeofence(
        @Query("lang") lang: String?,
        @Query("user_api_hash") user_api_hash: String?,
        @Part("name") name: RequestBody?,
        @Part("type") type: RequestBody?,
        @Part("polygon_color") polygon_color: RequestBody?,
        @Part("polygon") polygon_array: RequestBody?
    ): Response<BaseResponse>


    @Multipart
    @POST("edit_geofence")
    suspend fun editGeofence(
        @Query("lang") lang: String?,
        @Query("user_api_hash") user_api_hash: String?,
        @Part("id") id: RequestBody?,
        @Part("name") name: RequestBody?,
        @Part("type") type: RequestBody?,
        @Part("polygon_color") polygon_color: RequestBody?,
        @Part("polygon") polygon_array: RequestBody?
    ): Response<BaseResponse>


    @GET("get_tasks")
    suspend fun loadTask(
        @Query("lang") lang: String?,
        @Query("user_api_hash") userHash: String?
    ): Response<TaskListResponse>


    @POST("add_task")
    suspend fun addTask(
        @Query("lang") lang: String?,
        @Query("user_api_hash") userHash: String?,
        @Query("device_id") deviceId: Int?,
        @Query("title") title: String?,
        @Query("comment") comment: String?,
        @Query("priority") priority: Int?,
        @Query("status") status: Int?,
        @Query("pickup_address") pickup_address: String?,
        @Query("pickup_address_lat") pickup_address_lat: Double?,
        @Query("pickup_address_lng") pickup_address_lng: Double?,
        @Query("pickup_time_from") pickup_time_from: String?,
        @Query("pickup_time_to") pickup_time_to: String?,
        @Query("delivery_address") delivery_address: String?,
        @Query("delivery_address_lat") delivery_address_lat: Double?,
        @Query("delivery_address_lng") delivery_address_lng: Double?,
        @Query("delivery_time_from") delivery_time_from: String?,
        @Query("delivery_time_to") delivery_time_to: String?
    ): Response<BaseResponse>

    @GET("get_user_drivers")
    suspend fun getUserDrivers(
        @Query("lang") lang: String?,
        @Query("user_api_hash") userHash: String?
    ): Response<UserDriverResponse>

    @POST("add_user_driver")
    suspend fun addUserDriver(
        @Query("lang") lang: String?,
        @Query("user_api_hash") userHash: String?,
        @Query("device_id") deviceId: Int?,
        @Query("name") name: String?,
        @Query("rfid") rfid: String?,
        @Query("phone") phone: String?,
        @Query("email") email: String?,
        @Query("description") description: String?
    ): Response<BaseResponse>

    @GET("get_custom_events")
    suspend fun getCustomEvents(
        @Query("lang") lang: String?,
        @Query("user_api_hash") userHash: String?
    ): Response<CustomEventResponse>

    @GET("add_custom_event_data")
    suspend fun getCustomEventData(
        @Query("lang") lang: String?,
        @Query("user_api_hash") userHash: String?
    ): Response<ProtocolResponse>

    @GET("add_custom_event")
    suspend fun addCustomEvent(
        @Query("lang") lang: String?,
        @Query("user_api_hash") userHash: String?,
        @Query("protocol") protocol: String,
        @Query("message") message: String,
        @Query("show_always") show_always: Int,
        @Query("conditions") conditions_array: String
    ): Response<BaseResponse>


    @POST("edit_setup")
    suspend fun editSetup(
        @Query("lang") lang: String?,
        @Query("user_api_hash") userHash: String?,
        @Query("unit_of_distance") unit_of_distance: String,
        @Query("unit_of_capacity") unit_of_capacity: String?,
        @Query("unit_of_altitude") unit_of_altitude: String?,
        @Query("timezone_id") timezone_id: Int?,
        @Query("sms_gateway") sms_gateway: Int?,
        @Query("groups") groups: String?
    ): Response<BaseResponse>


    @GET("send_command_data")
    suspend fun getCommandData(
        @Query("lang") lang: String,
        @Query("user_api_hash") user_api_hash: String
    ): Response<SendCommandDataResponse>


    @GET("send_gprs_command")
    suspend fun sendGprsCommand(
        @Query("lang") lang: String,
        @Query("user_api_hash") user_api_hash: String,
        @Query("device_id") deviceId: Int,
        @Query("type") type: String,
        @Query("data") message: String
    ): Response<BaseResponse>


    @GET
    suspend fun sendSmsCommand(
        @Url url: String
    ): Response<BaseResponse>


    @GET("get_user_data")
    suspend fun getUserData(
        @Query("user_api_hash") user_api_hash: String
    ): Response<UserDataResponse>


    @GET("fcm_token")
    suspend fun saveFcmToken(
        @Query("user_api_hash") user_api_hash: String,
        @Query("token") token: String
    ): Response<BaseResponse>

    @GET("add_alert_data")
    suspend fun AddAlertData(
        @Query("lang") lang: String,
        @Query("user_api_hash") user_api_hash: String
    ): Response<AddAlertDataResponsee>

    @GET("get_custom_events_by_device")
    suspend fun getCustomEventsByDevice(
        @Query("lang") lang: String,
        @Query("user_api_hash") user_api_hash: String,
        @Query("devices[]") devices: String
    ): Response<GetCustomEventResponse>

    @GET
    suspend fun addAlert(
        @Url url: String
    ): Response<BaseResponse>

    @GET("get_alerts")
    suspend fun getAlerts(
        @Query("lang") lang: String,
        @Query("user_api_hash") user_api_hash: String
    ): Response<AlertResponse>

    @GET("destroy_alert")
    suspend fun destroyAlert(
        @Query("lang") lang: String,
        @Query("user_api_hash") user_api_hash: String,
        @Query("alert_id") alertId: Int
    ): Response<BaseResponse>

    @GET
    suspend fun editAlert(
        @Url url: String
    ): Response<BaseResponse>

    @GET("get_reports")
    suspend fun getReports(
        @Query("lang") lang: String,
        @Query("user_api_hash") user_api_hash: String,
        @Query("page") page: Int
    ): Response<ReportResponse>

    @GET("destroy_report")
    suspend fun destroyReport(
        @Query("lang") lang: String,
        @Query("user_api_hash") user_api_hash: String,
        @Query("report_id") reportId: Int
    ): Response<BaseResponse>


    @GET
    suspend fun saveReport(
        @Url url: String
    ): Response<BaseResponse>


    @GET("destroy_geofence")
    suspend fun destroyGeoFence(
        @Query("lang") lang: String,
        @Query("user_api_hash") user_api_hash: String,
        @Query("geofence_id") geofenceId: Int
    ): Response<BaseResponse>

    @GET("destroy_map_icon")
    suspend fun destroyPoiMarker(
        @Query("lang") lang: String,
        @Query("user_api_hash") user_api_hash: String,
        @Query("map_icon_id") geofenceId: Int
    ): Response<BaseResponse>

    @GET("edit_alert_data")
    suspend fun editAlertData(
        @Query("lang") lang: String,
        @Query("user_api_hash") user_api_hash: String,
        @Query("alert_id") alertId: Int
    ): Response<EditAlertResponse>


    @GET("get_device_commands")
    suspend fun getDeviceCommand(
        @Query("lang") lang: String,
        @Query("user_api_hash") user_api_hash: String,
        @Query("device_id") deviceId: Int
    ): Response<List<DeviceCommandResponse>>

    companion object {
        const val BASE = "https://go.shazcomgps.net/"
        const val BASE_URL = "https://go.shazcomgps.net/api/"
        operator fun invoke(client: OkHttpClient): GPSWoxAPI {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(GPSWoxAPI::class.java)
        }
    }
}