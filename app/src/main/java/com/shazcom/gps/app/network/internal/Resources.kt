package com.shazcom.gps.app.network.internal

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
</T> */

data class Resource<out T>(val status: Status, val data: T?, val message: String?, val code: Int?) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null, 0)
        }

        fun <T> error(msg: String, data: T?, code: Int? = 200): Resource<T> {
            return Resource(Status.ERROR, data, msg, code)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null, 0)
        }
    }
}