package com.shazcom.gps.app.ui.viewmodal

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.shazcom.gps.app.data.repository.CommonViewRepository
import com.shazcom.gps.app.data.response.*
import com.shazcom.gps.app.network.internal.Resource
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class CommonViewModel : ViewModel() {

    lateinit var commonViewRepository: CommonViewRepository

    fun getDeviceInfo(lang: String, userHash: String): LiveData<Resource<List<DeviceData>>> {
        return commonViewRepository.getDeviceInfo(lang, userHash)
    }

    fun getDeviceInfoLatest(lang: String, userHash: String): LiveData<Resource<LatestDeviceData>> {
        return commonViewRepository.getDeviceInfoLatest(lang, userHash)
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
        return commonViewRepository.getHistoryInfo(
            lang,
            userHash,
            deviceId,
            fromDate,
            fromTime,
            toDate,
            toTime
        )
    }

    fun getEvents(
        lang: String,
        userHash: String,
        deviceId: Int,
        page: Int
    ): LiveData<Resource<EventResponse>> {
        return commonViewRepository.getEvents(lang, userHash, deviceId, page)
    }

    fun getServices(
        lang: String,
        userHash: String,
        deviceId: Int,
        page: Int
    ): LiveData<Resource<ServiceResponse>> {
        return commonViewRepository.getServices(lang, userHash, deviceId, page)
    }

    fun addServices(
        lang: String,
        userHash: String,
        deviceId: Int,
        name: String,
        expBy: String,
        interval: String,
        lastService: String,
        triggerEventLeft: String,
        renewAfter: String,
        email: String
    ): LiveData<Resource<BaseResponse>> {

        val deviceIdBdy = deviceId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val nameBdy = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val expByBdy = expBy.toRequestBody("text/plain".toMediaTypeOrNull())
        val intervalBdy = interval.toRequestBody("text/plain".toMediaTypeOrNull())
        val lastServiceBdy = lastService.toRequestBody("text/plain".toMediaTypeOrNull())
        val renewAfterBdy = renewAfter.toRequestBody("text/plain".toMediaTypeOrNull())
        val triggerEventLeftBdy = triggerEventLeft.toRequestBody("text/plain".toMediaTypeOrNull())
        val emailBdy = email.toRequestBody("text/plain".toMediaTypeOrNull())

        return commonViewRepository.addService(
            lang,
            userHash,
            deviceId,
            deviceIdBdy,
            nameBdy,
            expByBdy,
            intervalBdy,
            lastServiceBdy,
            triggerEventLeftBdy,
            renewAfterBdy,
            emailBdy
        )
    }

    fun editServices(
        lang: String,
        userHash: String,
        id: Int,
        deviceId: Int,
        name: String,
        expBy: String,
        interval: String,
        lastService: String,
        triggerEventLeft: String,
        renewAfter: String,
        email: String
    ): LiveData<Resource<BaseResponse>> {

        val idBdy = id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val deviceIdBdy = deviceId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val nameBdy = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val expByBdy = expBy.toRequestBody("text/plain".toMediaTypeOrNull())
        val intervalBdy = interval.toRequestBody("text/plain".toMediaTypeOrNull())
        val lastServiceBdy = lastService.toRequestBody("text/plain".toMediaTypeOrNull())
        val renewAfterBdy = renewAfter.toRequestBody("text/plain".toMediaTypeOrNull())
        val triggerEventLeftBdy = triggerEventLeft.toRequestBody("text/plain".toMediaTypeOrNull())
        val emailBdy = email.toRequestBody("text/plain".toMediaTypeOrNull())

        return commonViewRepository.editService(
            lang,
            userHash,
            idBdy,
            deviceIdBdy,
            nameBdy,
            expByBdy,
            intervalBdy,
            lastServiceBdy,
            triggerEventLeftBdy,
            renewAfterBdy,
            emailBdy
        )
    }


    fun deleteService(
        lang: String,
        userHash: String,
        serviceId: Int
    ): LiveData<Resource<BaseResponse>> {
        return commonViewRepository.deleteEvents(lang, userHash, serviceId)
    }


    fun generateReport(
        lang: String,
        userHash: String,
        type: Int,
        format: String,
        devices: String,
        dateFrom: String,
        dateTo: String,
        email: String
    ): LiveData<Resource<BaseResponse>> {

        val typeBdy = type.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val formatBdy = format.toRequestBody("text/plain".toMediaTypeOrNull())
        val devicesBdy = devices.toRequestBody("text/plain".toMediaTypeOrNull())
        val dateFrmBdy = dateFrom.toRequestBody("text/plain".toMediaTypeOrNull())
        val dateToBdy = dateTo.toRequestBody("text/plain".toMediaTypeOrNull())
        val emailBdy = email.toRequestBody("text/plain".toMediaTypeOrNull())
        val showAddress = "true".toRequestBody("text/plain".toMediaTypeOrNull())
        val expenseTypeBdy = "all".toRequestBody("text/plain".toMediaTypeOrNull())
        val supplierBdy = "all".toRequestBody("text/plain".toMediaTypeOrNull())
        val ignitionOffBdy = "0".toRequestBody("text/plain".toMediaTypeOrNull())

        return commonViewRepository.generateReport(
            lang,
            userHash,
            typeBdy,
            formatBdy,
            devicesBdy,
            dateFrmBdy,
            dateToBdy,
            emailBdy,
            showAddress,
            expenseTypeBdy,
            supplierBdy,
            ignitionOffBdy
        )
    }


    fun addReport(
        lang: String,
        userHash: String,
        type: Int,
        format: String,
        devices: String,
        dateFrom: String,
        dateTo: String,
        email: String,
        daily : Int,
        weekly : Int,
        monthly : Int,
        dailyTime : String,
        weeklyTime : String,
        monthlyTime: String,
        title: String
    ): LiveData<Resource<BaseResponse>> {

        val typeBdy = type.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val formatBdy = format.toRequestBody("text/plain".toMediaTypeOrNull())
        val devicesBdy = devices.toRequestBody("text/plain".toMediaTypeOrNull())
        val dateFrmBdy = dateFrom.toRequestBody("text/plain".toMediaTypeOrNull())
        val dateToBdy = dateTo.toRequestBody("text/plain".toMediaTypeOrNull())
        val emailBdy = email.toRequestBody("text/plain".toMediaTypeOrNull())
        val showAddress = "true".toRequestBody("text/plain".toMediaTypeOrNull())
        val expenseTypeBdy = "all".toRequestBody("text/plain".toMediaTypeOrNull())
        val supplierBdy = "all".toRequestBody("text/plain".toMediaTypeOrNull())
        val ignitionOffBdy = "0".toRequestBody("text/plain".toMediaTypeOrNull())
        val dailyReq = daily.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val weeklyReq = weekly.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val monthlyReq = monthly.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val dailyTimeReq = dailyTime.toRequestBody("text/plain".toMediaTypeOrNull())
        val weeklyTimeReq = weeklyTime.toRequestBody("text/plain".toMediaTypeOrNull())
        val monthlyTimeReq = monthlyTime.toRequestBody("text/plain".toMediaTypeOrNull())
        val titleReq = title.toRequestBody("text/plain".toMediaTypeOrNull())

        return commonViewRepository.addReport(
            lang,
            userHash,
            typeBdy,
            formatBdy,
            devicesBdy,
            dateFrmBdy,
            dateToBdy,
            emailBdy,
            showAddress,
            dailyReq,
            expenseTypeBdy,
            supplierBdy,
            ignitionOffBdy,
            weeklyReq,
            monthlyReq,
            dailyTimeReq,
            weeklyTimeReq,
            monthlyTimeReq,
            titleReq
        )
    }


    fun getSetupData(
        lang: String,
        userHash: String
    ): LiveData<Resource<EditUserDataResponse>> {
        return commonViewRepository.getSetupData(lang, userHash)
    }

    fun editSetup( lang: String,
                   userHash: String,
                   unit_of_distance: String,
                   unit_of_capacity: String,
                   unit_of_altitude: String,
                   timezone_id: Int,
                   sms_gateway: Int,
                   groups: String): LiveData<Resource<BaseResponse>> {
        return commonViewRepository.editSetup( lang,
            userHash,
            unit_of_distance,
            unit_of_capacity,
            unit_of_altitude,
            timezone_id,
            sms_gateway,
            groups)
    }

}