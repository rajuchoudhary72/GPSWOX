package com.shazcom.gps.app.network.internal

import android.content.Context
import android.util.Log
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.utils.isInternetAvailable
import okhttp3.Interceptor
import okhttp3.Response
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

class NetworkInterceptor(context: Context) : Interceptor, KodeinAware {

    private val applicationContext = context.applicationContext
    override val kodein by closestKodein(applicationContext)
    private val localDB: LocalDB by instance()

    override fun intercept(chain: Interceptor.Chain): Response {

        if (!isInternetAvailable(applicationContext)) {
            throw NetworkConnectionException("Check your internet connection")
        }

        val request = chain.request()
        var proceed: Response? = null

        try {
            if (localDB.getToken() == null) {
                Log.e("Interceptor", "Chain")
                val requestBuilder = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept-Language", "en")
                    .addHeader("device-os", "android")
                    .addHeader("accept-version", "1.0.0")
                proceed = chain.proceed(requestBuilder.build())
            } else {
                val requestBuilder =
                    request.newBuilder()
                        .addHeader("Authorization", " ${localDB.getToken()}")
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Accept-Language", "en")
                        .addHeader("device-os", "android")
                        .addHeader("accept-version", "1.0.0")
                        .method(request.method, request.body)
                proceed = chain.proceed(requestBuilder.build())
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw NetworkConnectionException("Check your internet connection")
        }


        return proceed
    }
}