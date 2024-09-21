package com.shazcom.gps.app.ui.viewmodal

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.shazcom.gps.app.data.repository.AuthRepository
import com.shazcom.gps.app.data.response.AuthResponse
import com.shazcom.gps.app.data.response.BaseResponse
import com.shazcom.gps.app.data.response.UserDataResponse
import com.shazcom.gps.app.network.GPSWoxAPI
import com.shazcom.gps.app.network.internal.Resource
import com.shazcom.gps.app.network.request.UserAuth
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class AuthViewModel : ViewModel() {

    lateinit var authRepository: AuthRepository

    fun authUser(userAuth: UserAuth): LiveData<Resource<AuthResponse>> {
        val email = userAuth.userName.toRequestBody("text/plain".toMediaTypeOrNull())
        val password = userAuth.password.toRequestBody("text/plain".toMediaTypeOrNull())
        return authRepository.userAuthentication(email, password)
    }

    fun passwordReminder(email: String): LiveData<Resource<BaseResponse>> {
        val email = email.toRequestBody("text/plain".toMediaTypeOrNull())
        return authRepository.userPasswordReminder(
            "${GPSWoxAPI.BASE}password_reminder",
            email
        )
    }

    fun getUserData(user_api_hash: String): LiveData<Resource<UserDataResponse>> {
        return authRepository.getUserData(
            user_api_hash
        )
    }
}