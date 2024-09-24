package com.shazcom.gps.app.ui.viewmodal

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.shazcom.gps.app.data.repository.ToolsRepository
import com.shazcom.gps.app.data.response.*
import com.shazcom.gps.app.network.internal.Resource
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class ToolsViewModel : ViewModel() {

    lateinit var toolsRepository: ToolsRepository

    fun loadPoiMarkers(lang: String, userHash: String): LiveData<Resource<UserPoiResponse>> {
        return toolsRepository.loadPoiMarkers(lang, userHash)
    }

    fun loadPoiIcons(lang: String, userHash: String): LiveData<Resource<PoiIconsResponse>> {
        return toolsRepository.loadPoiIcons(lang, userHash)
    }

    fun savePOIMarker(
        lang: String, userHash: String, name: String,
        description: String,
        mapIconId: Int,
        location: String
    ): LiveData<Resource<BaseResponse>> {
        return toolsRepository.savePoiMarker(lang, userHash, name, description, mapIconId, location)
    }

    fun updatePOIMarker(
        lang: String,
        userHash: String,
        id: Int,
        name: String,
        description: String,
        mapIconId: Int,
        location: String
    ): LiveData<Resource<BaseResponse>> {
        return toolsRepository.updatePoiMarker(lang, userHash, id, name, description, mapIconId, location)
    }

    fun loadGeoFence(lang: String, userHash: String): LiveData<Resource<GeoFenceResponse>> {
        return toolsRepository.loadGeoFence(lang, userHash)
    }

    fun saveGeofence(
        lang: String, userHash: String, name: String,
        polygonColor: String,
        polygon: String?,
        type: String = "polygon",
        center: String? = null,
        radius: String? = null,
    ): LiveData<Resource<BaseResponse>> {

        val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val colorBody =  polygonColor.toRequestBody("text/plain".toMediaTypeOrNull())
        val geoBody =  polygon?.toRequestBody("text/plain".toMediaTypeOrNull())
        val typeBody =  type.toRequestBody("text/plain".toMediaTypeOrNull())
        val centerBody =  center?.toRequestBody("text/plain".toMediaTypeOrNull())
        val radiusBody =  radius?.toRequestBody("text/plain".toMediaTypeOrNull())

        return toolsRepository.saveGeoFence(lang, userHash, nameBody, colorBody, typeBody, geoBody,centerBody,radiusBody)
    }

    fun updateGeofence(
        lang: String,
        userHash: String,
        id: Int,
        name: String,
        polygonColor: String,
        polygon: String
    ): LiveData<Resource<BaseResponse>> {

        val idBody = id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val colorBody =  polygonColor.toRequestBody("text/plain".toMediaTypeOrNull())
        val geoBody =  polygon.toRequestBody("text/plain".toMediaTypeOrNull())
        val typeBody =  "polygon".toRequestBody("text/plain".toMediaTypeOrNull())

        return toolsRepository.updateGeoFence(lang, userHash, idBody,  nameBody, colorBody, typeBody, geoBody)
    }

    fun loadTask(lang: String, userHash: String): LiveData<Resource<TaskListResponse>> {
        return toolsRepository.loadTasks(lang, userHash)
    }

    fun addTask(
        lang: String,
        userHash: String,
        device: Int,
        title: String,
        comment: String,
        priority: Int,
        status: Int,
        pickup_address: String,
        pickup_address_lat: Double,
        pickup_address_lng: Double,
        pickup_time_from: String,
        pickup_time_to: String,
        delivery_address: String,
        delivery_address_lat: Double,
        delivery_address_lng: Double,
        delivery_time_from: String,
        delivery_time_to: String
    ): LiveData<Resource<BaseResponse>> {
        return toolsRepository.saveTask(
            lang,
            userHash,
            device,
            title,
            comment,
            priority,
            status,
            pickup_address,
            pickup_address_lat,
            pickup_address_lng,
            pickup_time_from,
            pickup_time_to,
            delivery_address,
            delivery_address_lat,
            delivery_address_lng,
            delivery_time_from,
            delivery_time_to
        )
    }


    fun loadDrivers(lang: String, userHash: String): LiveData<Resource<UserDriverResponse>> {
        return toolsRepository.loadDrivers(lang, userHash)
    }

    fun loadCustomEvents(lang: String, userHash: String): LiveData<Resource<CustomEventResponse>> {
        return toolsRepository.loadCustomEvents(lang, userHash)
    }

    fun loadProtocols(lang: String, userHash: String): LiveData<Resource<ProtocolResponse>> {
        return toolsRepository.loadProtocols(lang, userHash)
    }

    fun addDriver(
        lang: String,
        userHash: String,
        device_id: Int,
        name: String,
        rfid: String,
        phone: String,
        email: String,
        description: String
    ): LiveData<Resource<BaseResponse>> {
        return toolsRepository.addDriver(
            lang, userHash, device_id,
            name,
            rfid,
            phone,
            email,
            description
        )
    }

    fun addCustomEvent(
        lang: String,
        userHash: String,
        protocol: String,
        message: String,
        show_always: Int,
        conditions: String
    ): LiveData<Resource<BaseResponse>> {
        return toolsRepository.addCustomEvent(
            lang, userHash, protocol,
            message,
            show_always,
            conditions
        )
    }

    fun loadCommandData(
        lang: String,
        userHash: String
    ): LiveData<Resource<SendCommandDataResponse>> {
        return toolsRepository.loadCommandData(lang, userHash)
    }

    fun loadDeviceCommandData(
        lang: String,
        userHash: String,
        deviceId : Int
    ): LiveData<Resource<List<DeviceCommandResponse>>> {
        return toolsRepository.loadDeviceCommandData(lang, userHash, deviceId)
    }

    fun sendCommand(
        lang: String, userHash: String,
        device_id: Int,
        type: String,
        message: String
    ): LiveData<Resource<BaseResponse>> {
        return toolsRepository.sendCommand(
            lang, userHash, device_id,
            type,
            message
        )
    }

    fun sendSmsCommand (
        url: String
    ): LiveData<Resource<BaseResponse>> {
        return toolsRepository.sendSmsCommand(url)
    }

    fun saveToken(user_hash_api: String, token: String): LiveData<Resource<BaseResponse>> {
        return toolsRepository.saveFcmToken(user_hash_api, token)
    }

    fun loadAlertData(
        lang: String,
        userHash: String
    ): LiveData<Resource<AddAlertDataResponsee>> {
        return toolsRepository.loadAlertData(lang, userHash)
    }

    fun getCustomEventsByDevice(
        lang: String,
        userHash: String,
        device: String
    ): LiveData<Resource<GetCustomEventResponse>> {
        return toolsRepository.getCustomEventByDevice(lang, userHash, device)
    }

    fun addAlert(
        url: String
    ): LiveData<Resource<BaseResponse>> {
        return toolsRepository.addAlert(url)
    }

    fun loadAlerts(lang: String, userHash: String): LiveData<Resource<AlertResponse>> {
        return toolsRepository.loadAlerts(lang, userHash)
    }

    fun editAlert(
        url: String
    ): LiveData<Resource<BaseResponse>> {
        return toolsRepository.editAlert(url)
    }

    fun destroyAlert(
        lang: String,
        userHash: String,
        alertId: Int
    ): LiveData<Resource<BaseResponse>> {
        return toolsRepository.deleteAlert(lang, userHash, alertId)
    }

    fun getReports(
        lang: String,
        userHash: String,
        page: Int
    ): LiveData<Resource<ReportResponse>> {
        return toolsRepository.getReports(lang, userHash, page)
    }


    fun destroyReport(
        lang: String,
        userHash: String,
        reportId: Int
    ): LiveData<Resource<BaseResponse>> {
        return toolsRepository.deleteReport(lang, userHash, reportId)
    }

    fun saveReport(
        url: String
    ): LiveData<Resource<BaseResponse>> {
        return toolsRepository.saveReport(url)
    }


    fun destroyGeoFence(
        lang: String,
        userHash: String,
        geoFenceId : Int
    ): LiveData<Resource<BaseResponse>> {
        return toolsRepository.deleteGeoFence(lang, userHash, geoFenceId)
    }

    fun destroyPoiMarker(
        lang: String,
        userHash: String,
        poiMarkerId : Int
    ): LiveData<Resource<BaseResponse>> {
        return toolsRepository.deletePoiMarker(lang, userHash, poiMarkerId)
    }


    fun getEditAlert(
        lang: String,
        userHash: String,
        alertId: Int
    ): LiveData<Resource<EditAlertResponse>> {
        return toolsRepository.getEditAlert(lang, userHash, alertId)
    }
}