package com.shazcom.gps.app.data.repository

import androidx.lifecycle.LiveData
import com.shazcom.gps.app.data.response.*
import com.shazcom.gps.app.network.GPSWoxAPI
import com.shazcom.gps.app.network.internal.ApiRequest
import com.shazcom.gps.app.network.internal.Resource
import kotlinx.coroutines.*
import okhttp3.RequestBody

class ToolsRepository(val gpsWoxAPI: GPSWoxAPI) : ApiRequest() {

    var job: CompletableJob? = null

    fun loadPoiMarkers(
        lang: String,
        userHash: String
    ): LiveData<Resource<UserPoiResponse>> {
        job = Job()
        return object : LiveData<Resource<UserPoiResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest { gpsWoxAPI.loadPOIMarkers(lang, userHash) }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }

    fun loadPoiIcons(
        lang: String,
        userHash: String
    ): LiveData<Resource<PoiIconsResponse>> {
        job = Job()
        return object : LiveData<Resource<PoiIconsResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest { gpsWoxAPI.getPOIMapIcons(lang, userHash) }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }


    fun savePoiMarker(
        lang: String,
        userHash: String,
        name: String,
        description: String,
        mapIconId: Int,
        location: String
    ): LiveData<Resource<BaseResponse>> {
        job = Job()
        return object : LiveData<Resource<BaseResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest {
                            gpsWoxAPI.savePOIMarker(
                                lang,
                                userHash,
                                name,
                                description,
                                mapIconId,
                                location
                            )
                        }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }

    fun loadGeoFence(
        lang: String,
        userHash: String
    ): LiveData<Resource<GeoFenceResponse>> {
        job = Job()
        return object : LiveData<Resource<GeoFenceResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest { gpsWoxAPI.loadGeoFence(lang, userHash) }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }


    fun saveGeoFence(
        lang: String,
        userHash: String,
        name: RequestBody,
        polygonColor: RequestBody,
        type : RequestBody,
        polygon: RequestBody
    ): LiveData<Resource<BaseResponse>> {
        job = Job()
        return object : LiveData<Resource<BaseResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest {
                            gpsWoxAPI.addNewGeofence(
                                lang,
                                userHash,
                                name,
                                type,
                                polygonColor,
                                polygon
                            )
                        }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }


    fun loadTasks(
        lang: String,
        userHash: String
    ): LiveData<Resource<TaskListResponse>> {
        job = Job()
        return object : LiveData<Resource<TaskListResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest { gpsWoxAPI.loadTask(lang, userHash) }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }


    fun saveTask(
        lang: String,
        userHash: String,
        deviceId: Int?,
        title: String?,
        comment: String?,
        priority: Int?,
        status: Int?,
        pickup_address: String?,
        pickup_address_lat: Double?,
        pickup_address_lng: Double?,
        pickup_time_from: String?,
        pickup_time_to: String?,
        delivery_address: String?,
        delivery_address_lat: Double?,
        delivery_address_lng: Double?,
        delivery_time_from: String?,
        delivery_time_to: String?
    ): LiveData<Resource<BaseResponse>> {
        job = Job()
        return object : LiveData<Resource<BaseResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest {
                            gpsWoxAPI.addTask(
                                lang,
                                userHash,
                                deviceId,
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
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }


    fun loadDrivers(
        lang: String,
        userHash: String
    ): LiveData<Resource<UserDriverResponse>> {
        job = Job()
        return object : LiveData<Resource<UserDriverResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest { gpsWoxAPI.getUserDrivers(lang, userHash) }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }

    fun loadCustomEvents(
        lang: String,
        userHash: String
    ): LiveData<Resource<CustomEventResponse>> {
        job = Job()
        return object : LiveData<Resource<CustomEventResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest { gpsWoxAPI.getCustomEvents(lang, userHash) }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }

    fun loadProtocols(
        lang: String,
        userHash: String
    ): LiveData<Resource<ProtocolResponse>> {
        job = Job()
        return object : LiveData<Resource<ProtocolResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest { gpsWoxAPI.getCustomEventData(lang, userHash) }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
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

        job = Job()
        return object : LiveData<Resource<BaseResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest {
                            gpsWoxAPI.addUserDriver(
                                lang, userHash, device_id,
                                name,
                                rfid,
                                phone,
                                email,
                                description
                            )
                        }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }


    fun addCustomEvent(
        lang: String,
        userHash: String,
        protocol: String,
        message: String,
        show_always: Int,
        conditions: String
    ): LiveData<Resource<BaseResponse>> {

        job = Job()
        return object : LiveData<Resource<BaseResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest {
                            gpsWoxAPI.addCustomEvent(
                                lang, userHash, protocol,
                                message,
                                show_always,
                                conditions
                            )
                        }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }


    fun loadCommandData(
        lang: String,
        userHash: String
    ): LiveData<Resource<SendCommandDataResponse>> {
        job = Job()
        return object : LiveData<Resource<SendCommandDataResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest { gpsWoxAPI.getCommandData(lang, userHash) }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }

    fun loadDeviceCommandData(
        lang: String,
        userHash: String,
        deviceId: Int
    ): LiveData<Resource<List<DeviceCommandResponse>>> {
        job = Job()
        return object : LiveData<Resource<List<DeviceCommandResponse>>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest { gpsWoxAPI.getDeviceCommand(lang, userHash, deviceId) }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }


    fun sendCommand(
        lang: String,
        userHash: String,
        device_id: Int,
        type: String,
        message: String
    ): LiveData<Resource<BaseResponse>> {
        job = Job()
        return object : LiveData<Resource<BaseResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest {
                            gpsWoxAPI.sendGprsCommand(
                                lang,
                                userHash,
                                device_id,
                                type,
                                message
                            )
                        }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }

    fun saveFcmToken(userHashApi: String, token: String): LiveData<Resource<BaseResponse>> {
        job = Job()
        return object : LiveData<Resource<BaseResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest {
                            gpsWoxAPI.saveFcmToken(
                                userHashApi, token
                            )
                        }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }

    fun loadAlertData(lang: String, userHash: String): LiveData<Resource<AddAlertDataResponsee>> {
        job = Job()
        return object : LiveData<Resource<AddAlertDataResponsee>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest { gpsWoxAPI.AddAlertData(lang, userHash) }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }

    fun getCustomEventByDevice(
        lang: String,
        userHash: String,
        devices: String
    ): LiveData<Resource<GetCustomEventResponse>> {
        job = Job()
        return object : LiveData<Resource<GetCustomEventResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest {
                            gpsWoxAPI.getCustomEventsByDevice(
                                lang,
                                userHash,
                                devices
                            )
                        }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }

    fun addAlert(url: String): LiveData<Resource<BaseResponse>> {
        job = Job()
        return object : LiveData<Resource<BaseResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest { gpsWoxAPI.addAlert(url) }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }

    fun loadAlerts(
        lang: String,
        userHash: String
    ): LiveData<Resource<AlertResponse>> {
        job = Job()
        return object : LiveData<Resource<AlertResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest { gpsWoxAPI.getAlerts(lang, userHash) }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }


    fun editAlert(url: String): LiveData<Resource<BaseResponse>> {
        job = Job()
        return object : LiveData<Resource<BaseResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest { gpsWoxAPI.editAlert(url) }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }

    fun deleteAlert(
        lang: String,
        userHash: String,
        alertId: Int
    ): LiveData<Resource<BaseResponse>> {
        job = Job()
        return object : LiveData<Resource<BaseResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest {
                            gpsWoxAPI.destroyAlert(
                                lang,
                                userHash,
                                alertId
                            )
                        }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }

    fun getReports(lang: String, userHash: String, page: Int)
            : LiveData<Resource<ReportResponse>> {
        job = Job()
        return object : LiveData<Resource<ReportResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest {
                            gpsWoxAPI.getReports(
                                lang,
                                userHash,
                                page
                            )
                        }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }

    }

    fun deleteReport(
        lang: String,
        userHash: String,
        reportId: Int
    ): LiveData<Resource<BaseResponse>> {
        job = Job()
        return object : LiveData<Resource<BaseResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest {
                            gpsWoxAPI.destroyReport(
                                lang,
                                userHash,
                                reportId
                            )
                        }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }


    fun saveReport(url: String): LiveData<Resource<BaseResponse>> {
        job = Job()
        return object : LiveData<Resource<BaseResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest { gpsWoxAPI.saveReport(url) }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }


    fun sendSmsCommand(url: String): LiveData<Resource<BaseResponse>> {
        job = Job()
        return object : LiveData<Resource<BaseResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest { gpsWoxAPI.sendSmsCommand(url) }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }


    fun deleteGeoFence(
        lang: String,
        userHash: String,
        geoFenceId : Int
    ): LiveData<Resource<BaseResponse>> {
        job = Job()
        return object : LiveData<Resource<BaseResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest {
                            gpsWoxAPI.destroyGeoFence(
                                lang,
                                userHash,
                                geoFenceId
                            )
                        }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }



    fun deletePoiMarker(
        lang: String,
        userHash: String,
        poiMarkerId : Int
    ): LiveData<Resource<BaseResponse>> {
        job = Job()
        return object : LiveData<Resource<BaseResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest {
                            gpsWoxAPI.destroyPoiMarker(
                                lang,
                                userHash,
                                poiMarkerId
                            )
                        }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }


    fun updateGeoFence(
        lang: String,
        userHash: String,
        id: RequestBody,
        name: RequestBody,
        polygonColor: RequestBody,
        type : RequestBody,
        polygon: RequestBody
    ): LiveData<Resource<BaseResponse>> {
        job = Job()
        return object : LiveData<Resource<BaseResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest {
                            gpsWoxAPI.editGeofence(
                                lang,
                                userHash,
                                id,
                                name,
                                type,
                                polygonColor,
                                polygon
                            )
                        }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }


    fun updatePoiMarker(
        lang: String,
        userHash: String,
        id: Int,
        name: String,
        description: String,
        mapIconId: Int,
        location: String
    ): LiveData<Resource<BaseResponse>> {
        job = Job()
        return object : LiveData<Resource<BaseResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest {
                            gpsWoxAPI.updatePOIMarker(
                                lang,
                                userHash,
                                id,
                                name,
                                description,
                                mapIconId,
                                location
                            )
                        }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }


    fun getEditAlert(lang: String, userHash: String, alertId: Int)
            : LiveData<Resource<EditAlertResponse>> {
        job = Job()
        return object : LiveData<Resource<EditAlertResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) return
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest {
                            gpsWoxAPI.editAlertData(
                                lang,
                                userHash,
                                alertId
                            )
                        }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }

    }

}

