package com.shazcom.gps.app.network.internal

import com.shazcom.gps.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier

object Client {
    operator fun invoke(networkInterceptor: NetworkInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor()

        if (BuildConfig.DEBUG) {
            logging.level = HttpLoggingInterceptor.Level.BODY
        } else {
            logging.level = HttpLoggingInterceptor.Level.NONE
        }


        return OkHttpClient.Builder()
            .addInterceptor(networkInterceptor)
            .addInterceptor(logging)
            .hostnameVerifier(getHostnameVerifier())
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .connectTimeout(1, TimeUnit.MINUTES)
            .retryOnConnectionFailure(true)
            .protocols(listOf(Protocol.HTTP_1_1))
            .build()
    }

    private fun getHostnameVerifier(): HostnameVerifier {
        return HostnameVerifier { hostname, session ->
            true // hostname.contains("go.shazcomgps.net")
        }
    }
}