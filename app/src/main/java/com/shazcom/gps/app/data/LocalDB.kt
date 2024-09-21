package com.shazcom.gps.app.data

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.shazcom.gps.app.ui.BaseActivity
import com.shazcom.gps.app.ui.activities.LoginActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shazcom.gps.app.data.vo.NotificationItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class LocalDB(private val context: Context) {

    private val TOKEN = "token"
    private val FCM_TOKEN = "fcm_token"
    private val MAP_TYPE = "map_type"
    private val MAP_GEO_FENCE = "map_geofence"
    private val MAP_POI = "map_poi"
    private val MAP_TAIL = "map_tail"
    private val APP_LANG = "lang"
    private val DEVICE_ITEM = "device_item"
    private var sharedPreferences: SharedPreferences? = null

    init {
        sharedPreferences = context.getSharedPreferences("GPSWoxApp", Context.MODE_PRIVATE)
    }

    fun saveToken(token: String) {
        sharedPreferences?.edit()?.putString(TOKEN, token)?.apply()
    }

    fun getToken(): String? {
        return sharedPreferences?.getString(TOKEN, null)
    }

    fun saveFcmToken(token: String) {
        sharedPreferences?.edit()?.putString(FCM_TOKEN, token)?.apply()
    }

    fun getFcmToken(): String? {
        return sharedPreferences?.getString(FCM_TOKEN, null)
    }

    fun saveMapType(type: String) {
        sharedPreferences?.edit()?.putString(MAP_TYPE, type)?.apply()
    }

    fun getMapType(): String? {
        return sharedPreferences?.getString(MAP_TYPE, "MAP_TYPE_NORMAL")
    }

    fun flipGeofenceFlag() {
        sharedPreferences?.edit()?.putBoolean(MAP_GEO_FENCE, !isGeofenceFlag())?.apply()
    }

    fun flipPOIFlag() {
        sharedPreferences?.edit()?.putBoolean(MAP_POI, !isPOIFlag())?.apply()
    }

    fun flipTailFlag() {
        sharedPreferences?.edit()?.putBoolean(MAP_TAIL, !isTailFlag())?.apply()
    }

    fun isGeofenceFlag(): Boolean {
        return sharedPreferences?.getBoolean(MAP_GEO_FENCE, false)!!
    }

    fun isPOIFlag(): Boolean {
        return sharedPreferences?.getBoolean(MAP_POI, false)!!
    }

    fun isTailFlag(): Boolean {
        return sharedPreferences?.getBoolean(MAP_TAIL, true)!!
    }

    fun removeAll(activity: BaseActivity) {
        CoroutineScope(Dispatchers.IO).launch {
            try{
                FirebaseApp.initializeApp(context)
                FirebaseInstanceId.getInstance().deleteInstanceId()
            }catch (ex: Exception) {
                ex.printStackTrace()
            }finally {
                withContext(Dispatchers.Main) {
                    sharedPreferences?.edit()?.clear()?.apply()

                    Intent(context , LoginActivity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        activity.startActivity(it)
                        activity.finish()
                    }

                }
            }
        }
    }

    fun setLanguage(lang: String) {
        sharedPreferences?.edit()?.putString(APP_LANG, lang)?.apply()
    }

    fun getLanguage(): String? {
        return sharedPreferences?.getString(APP_LANG, "en")
    }


    fun saveNotificationList(data: String?) {
        sharedPreferences?.edit()?.putString(DEVICE_ITEM, data)?.apply()
    }

    fun getDeviceList(): List<NotificationItem>? {
        var listItem: List<NotificationItem>? = null
        val data = sharedPreferences?.getString(DEVICE_ITEM, "")
        data?.let {
            it.isNotEmpty()?.let {
                listItem =
                    Gson().fromJson(data, object : TypeToken<List<NotificationItem>>() {}.type)
            }
        }

        return listItem
    }
}