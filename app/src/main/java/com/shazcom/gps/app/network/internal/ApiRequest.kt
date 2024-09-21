package com.shazcom.gps.app.network.internal

import kotlinx.coroutines.delay
import org.json.JSONObject
import retrofit2.Response
import java.lang.Exception

abstract class  ApiRequest {

    suspend fun <T : Any> apiRequest(call: suspend () -> Response<T>): Resource<T> {

        try {
            val response = call.invoke()
            if (response.isSuccessful) {
                delay(300)
                return Resource.success(response.body()!!)
            } else {
                delay(300)
                return try {
                    val json = JSONObject(response.errorBody()!!.string())
                    Resource.error(json.getString("message"), null, response.code())
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    val error = response.errorBody()?.string()
                    if (error.isNullOrBlank()) {
                        Resource.error("Something went wrong, please try again later", null)
                    } else {
                        Resource.error(error.toString(), null)
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            delay(500)
            return Resource.error(ex.message.toString(), null)
        }

    }
}