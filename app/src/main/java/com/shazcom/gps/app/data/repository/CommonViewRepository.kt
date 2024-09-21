package com.shazcom.gps.app.data.repository

import androidx.lifecycle.LiveData
import com.shazcom.gps.app.data.response.*
import com.shazcom.gps.app.network.GPSWoxAPI
import com.shazcom.gps.app.network.internal.ApiRequest
import com.shazcom.gps.app.network.internal.Resource
import kotlinx.coroutines.*
import okhttp3.RequestBody

class CommonViewRepository(private val gpsWoxAPI: GPSWoxAPI) : ApiRequest() {

    var job: CompletableJob? = null
    fun getDeviceInfo(lang: String, userHash: String): LiveData<Resource<List<DeviceData>>> {
        job = Job()
        return object : LiveData<Resource<List<DeviceData>>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) {
                        return
                    }
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest { gpsWoxAPI.getDevices(lang, userHash) }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }


    fun getDeviceInfoLatest(lang: String, userHash: String): LiveData<Resource<LatestDeviceData>> {
        job = Job()
        return object : LiveData<Resource<LatestDeviceData>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) {
                        return
                    }
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest { gpsWoxAPI.getDevicesLatest(lang, userHash) }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }


    fun getHistoryInfo(
        lang: String,
        userHash: String,
        deviceId: Int,
        fromDate: String,
        fromTime: String,
        toDate: String,
        toTime: String
    ): LiveData<Resource<HistoryData>> {
        job = Job()
        return object : LiveData<Resource<HistoryData>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) {
                        return
                    }
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest {
                            gpsWoxAPI.getHistory(
                                lang,
                                userHash,
                                deviceId,
                                fromDate,
                                fromTime,
                                toDate,
                                toTime
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

    fun getEvents(
        lang: String,
        userHash: String,
        deviceId: Int,
        page: Int
    ): LiveData<Resource<EventResponse>> {
        job = Job()
        return object : LiveData<Resource<EventResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) {
                        return
                    }
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response =
                            apiRequest { gpsWoxAPI.getEvents(lang, userHash, deviceId, page) }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }

    fun getServices(
        lang: String,
        userHash: String,
        deviceId: Int,
        page: Int
    ): LiveData<Resource<ServiceResponse>> {
        job = Job()
        return object : LiveData<Resource<ServiceResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) {
                        return
                    }
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response =
                            apiRequest { gpsWoxAPI.getServices(lang, userHash, deviceId, page) }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }


    fun addService(
        lang: String,
        userHash: String,
        deviceId: Int,
        device_id: RequestBody,
        name: RequestBody,
        expBy: RequestBody,
        interval: RequestBody,
        lastService: RequestBody,
        triggerEvent: RequestBody,
        renew: RequestBody,
        email: RequestBody
    ): LiveData<Resource<BaseResponse>> {
        job = Job()
        return object : LiveData<Resource<BaseResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest {
                            gpsWoxAPI.addService(
                                lang,
                                userHash,
                                deviceId,
                                device_id,
                                name,
                                expBy,
                                interval,
                                lastService,
                                triggerEvent,
                                renew,
                                email
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


    fun editService(
        lang: String,
        userHash: String,
        id: RequestBody,
        device_id: RequestBody,
        name: RequestBody,
        expBy: RequestBody,
        interval: RequestBody,
        lastService: RequestBody,
        triggerEvent: RequestBody,
        renew: RequestBody,
        email: RequestBody
    ): LiveData<Resource<BaseResponse>> {
        job = Job()
        return object : LiveData<Resource<BaseResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest {
                            gpsWoxAPI.editService(
                                lang,
                                userHash,
                                id,
                                device_id,
                                name,
                                expBy,
                                interval,
                                lastService,
                                triggerEvent,
                                renew,
                                email
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

    fun deleteEvents(
        lang: String,
        userHash: String,
        serviceId: Int
    ): LiveData<Resource<BaseResponse>> {
        job = Job()
        return object : LiveData<Resource<BaseResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest {
                            gpsWoxAPI.deleteService(
                                lang, userHash, serviceId
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

    fun generateReport(
        lang: String,
        userHash: String,
        type: RequestBody,
        format: RequestBody,
        deviceArray: RequestBody,
        date_from: RequestBody,
        date_to: RequestBody,
        sendEmail: RequestBody,
        showAddress : RequestBody,
        expenseTypeBdy : RequestBody,
        supplierBdy : RequestBody ,
        ignitionOffBdy : RequestBody
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
                            gpsWoxAPI.generateReport(
                                lang,
                                userHash,
                                type, format, deviceArray, date_from, date_to, sendEmail, showAddress, expenseTypeBdy,
                                supplierBdy,
                                ignitionOffBdy
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



    fun addReport(
        lang: String,
        userHash: String,
        type: RequestBody,
        format: RequestBody,
        deviceArray: RequestBody,
        date_from: RequestBody,
        date_to: RequestBody,
        sendEmail: RequestBody,
        showAddress : RequestBody,
        dailyReq : RequestBody,
        expenseTypeBdy : RequestBody,
        supplierBdy : RequestBody ,
        ignitionOffBdy : RequestBody,
        weekly : RequestBody,
        monthly : RequestBody,
        dailyTime : RequestBody,
        weeklyTime : RequestBody,
        monthlyTime: RequestBody,
        title: RequestBody
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
                            gpsWoxAPI.addReport(
                                lang,
                                userHash,
                                type, format, deviceArray, date_from,
                                date_to, sendEmail, showAddress, dailyReq,
                                expenseTypeBdy,
                                supplierBdy,
                                ignitionOffBdy,
                                weekly, monthly, dailyTime, weeklyTime, monthlyTime, title
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


    fun getSetupData(
        lang: String,
        userHash: String
    ): LiveData<Resource<EditUserDataResponse>> {
        job = Job()
        return object : LiveData<Resource<EditUserDataResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) {
                        return
                    }
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response =
                            apiRequest { gpsWoxAPI.edtSetupData(lang, userHash) }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }


    fun editSetup(
        lang: String,
        userHash: String,
        unit_of_distance: String,
        unit_of_capacity: String,
        unit_of_altitude: String,
        timezone_id: Int,
        sms_gateway: Int,
        groups: String
    ): LiveData<Resource<BaseResponse>> {
        job = Job()
        return object : LiveData<Resource<BaseResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if (theJob.isCompleted) {
                        return
                    }
                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response =
                            apiRequest {
                                gpsWoxAPI.editSetup(
                                    lang,
                                    userHash,
                                    unit_of_distance,
                                    unit_of_capacity,
                                    unit_of_altitude,
                                    timezone_id,
                                    sms_gateway,
                                    groups
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