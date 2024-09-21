package com.shazcom.gps.app

import android.app.Application
import android.os.StrictMode
import android.util.Log
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.AuthRepository
import com.shazcom.gps.app.data.repository.CommonViewRepository
import com.shazcom.gps.app.data.repository.ToolsRepository
import com.shazcom.gps.app.data.response.DeviceData
import com.shazcom.gps.app.data.response.GeoFenceData
import com.shazcom.gps.app.data.response.ItemsInner
import com.shazcom.gps.app.data.response.MapIcons
import com.shazcom.gps.app.network.GPSWoxAPI
import com.shazcom.gps.app.network.internal.Client
import com.shazcom.gps.app.network.internal.NetworkInterceptor
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.shazcom.gps.app.data.vo.NotificationItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.androidCoreModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

class GPSWoxApp : Application(), KodeinAware {

    private var itemsInner: ItemsInner? = null
    private var deviceDataList: List<DeviceData>? = null
    private var geofences: List<GeoFenceData>? = null
    private var mapIcons: List<MapIcons>? = null

    private val localDB: LocalDB by instance<LocalDB>()

    var appIsRunning = false
    var appKilled = false

    override fun onCreate() {
        super.onCreate()

        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
    }

    override val kodein = Kodein.lazy {

        import(androidCoreModule(this@GPSWoxApp))
        bind() from this.singleton { LocalDB(this.instance()) }

        bind() from this.singleton { NetworkInterceptor(this.instance()) }
        bind() from this.singleton { Client(this.instance()) }
        bind() from this.singleton { GPSWoxAPI(this.instance()) }

        bind() from this.singleton { AuthRepository(this.instance()) }
        bind() from this.singleton { CommonViewRepository(this.instance()) }
        bind() from this.singleton { ToolsRepository(this.instance()) }

        FirebaseApp.initializeApp(this@GPSWoxApp)
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("TrackProGps", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token
                localDB.saveFcmToken(token!!)

                // Log and toast
                val msg = getString(R.string.msg_token_fmt, token)
                Log.d("TrackProGps", msg)
            })
    }

    fun setInnerItem(item: ItemsInner?) {
        itemsInner = item
    }

    fun getItemInner(): ItemsInner? {
        return itemsInner
    }

    fun saveAllDevices(deviceList: List<DeviceData>) {
        deviceDataList = deviceList


        CoroutineScope(Dispatchers.IO).launch {
            val notificationList = ArrayList<NotificationItem>()
            deviceList.forEach { deviceData ->
                deviceData.items.forEach { item ->
                    item?.id?.let {
                        notificationList.add(NotificationItem(item.id.toString(), item.name.toString()))
                    }
                }
            }.also {
                notificationList.isNotEmpty().let {
                    val data = Gson().toJson(notificationList)
                    localDB.saveNotificationList(data)
                }
            }
        }
    }

    fun getDeviceList(): List<DeviceData>? {
        return deviceDataList
    }

    fun saveGeofence(geofences: List<GeoFenceData>?) {
        this.geofences = geofences
    }

    fun getGeoFence(): List<GeoFenceData>? {
        return this.geofences
    }

    fun savePoiMarkers(mapIcons: List<MapIcons>?) {
        this.mapIcons = mapIcons
    }

    fun getPoiMarkers(): List<MapIcons>? {
        return this.mapIcons
    }
}