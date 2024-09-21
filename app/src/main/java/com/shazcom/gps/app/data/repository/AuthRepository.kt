package com.shazcom.gps.app.data.repository

import androidx.lifecycle.LiveData
import com.shazcom.gps.app.data.response.AuthResponse
import com.shazcom.gps.app.data.response.BaseResponse
import com.shazcom.gps.app.data.response.UserDataResponse
import com.shazcom.gps.app.network.GPSWoxAPI
import com.shazcom.gps.app.network.internal.ApiRequest
import com.shazcom.gps.app.network.internal.Resource
import kotlinx.coroutines.*
import okhttp3.RequestBody

class AuthRepository(val gpsWoxAPI: GPSWoxAPI) : ApiRequest() {

    var job: CompletableJob? = null
    fun userAuthentication(
        email: RequestBody,
        password: RequestBody
    ): LiveData<Resource<AuthResponse>> {
        job = Job()
        return object : LiveData<Resource<AuthResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->

                    if(theJob.isCompleted) return

                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest { gpsWoxAPI.userAuth(email, password) }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }


    fun userPasswordReminder(
        url: String,
        email: RequestBody
    ): LiveData<Resource<BaseResponse>> {
        job = Job()
        return object : LiveData<Resource<BaseResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->
                    if(theJob.isCompleted) return

                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest { gpsWoxAPI.forgotPassword(url, email) }
                        withContext(Dispatchers.Main) {
                            value = response
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }

    fun getUserData(userApiHash: String): LiveData<Resource<UserDataResponse>> {
        job = Job()
        return object : LiveData<Resource<UserDataResponse>>() {
            override fun onActive() {
                super.onActive()
                job?.let { theJob ->

                    if(theJob.isCompleted) return

                    value = Resource.loading(null)
                    CoroutineScope(Dispatchers.IO + theJob).launch {
                        val response = apiRequest { gpsWoxAPI.getUserData(userApiHash) }
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