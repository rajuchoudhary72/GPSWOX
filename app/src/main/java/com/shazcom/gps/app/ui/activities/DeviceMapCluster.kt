package com.shazcom.gps.app.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.shazcom.gps.app.GPSWoxApp
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.vo.MapClusterItem
import com.shazcom.gps.app.ui.BaseActivity
import com.shazcom.gps.app.utils.DeviceMarkerRender
import com.shazcom.gps.app.utils.getMapTypes
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.LatLngBounds
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import kotlinx.android.synthetic.main.activity_map_cluster.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import kotlin.math.floor


class DeviceMapCluster : BaseActivity(), OnMapReadyCallback, KodeinAware,
    ClusterManager.OnClusterClickListener<MapClusterItem>,
    ClusterManager.OnClusterItemClickListener<MapClusterItem>,
    ClusterManager.OnClusterItemInfoWindowClickListener<MapClusterItem>,
    GoogleMap.OnCameraMoveListener {

    private var mClusterManager: ClusterManager<MapClusterItem>? = null
    private var mMap: GoogleMap? = null
    override val kodein by kodein()
    private val localDB: LocalDB by instance()
    private var app: GPSWoxApp? = null
    private var currentZoomLevel = 3f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_cluster)
        app = application as GPSWoxApp
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        closeBtn.setOnClickListener { finish() }
    }

    override fun onMapReady(googleMap: GoogleMap?) {

        googleMap?.mapType = getMapTypes(localDB?.getMapType()!!)
        googleMap?.uiSettings?.isMapToolbarEnabled = true
        googleMap?.uiSettings?.setAllGesturesEnabled(true)
        googleMap?.uiSettings?.isTiltGesturesEnabled = true
        googleMap?.uiSettings?.isRotateGesturesEnabled = true

        mMap = googleMap
        mMap?.setOnCameraMoveListener(this)

        mClusterManager = ClusterManager(this@DeviceMapCluster, googleMap)

        val renderer = DeviceMarkerRender(this, mMap!!, mClusterManager!!)
        mClusterManager?.renderer = renderer

        mMap?.setOnMarkerClickListener(mClusterManager)

        mMap?.setOnInfoWindowClickListener(mClusterManager)
        mClusterManager?.setOnClusterClickListener(this)
        mClusterManager?.setOnClusterItemClickListener(this)
        mClusterManager?.setOnClusterItemInfoWindowClickListener(this)

        mClusterManager?.clusterMarkerCollection?.setOnInfoWindowAdapter(mClusterManager?.markerManager)
        mClusterManager?.markerCollection?.setOnInfoWindowAdapter(mClusterManager?.markerManager)

        val type = intent?.extras?.getString("type", "all") ?: "all"
        populateClusters(type)

        initClicks()


    }

    private fun initClicks() {

        zoomIn.setOnClickListener {
            mMap?.animateCamera(CameraUpdateFactory.zoomIn())
            if (currentZoomLevel < mMap?.maxZoomLevel!!) {
                currentZoomLevel += 1
            }
        }
        zoomOut.setOnClickListener {
            mMap?.animateCamera(CameraUpdateFactory.zoomOut())
            if (currentZoomLevel > mMap?.minZoomLevel!!) {
                currentZoomLevel -= 1
            }
        }

        mapType.setOnClickListener {
            if (mapTypeLayout.isVisible) {
                mapTypeLayout.visibility = View.GONE
            } else {
                mapTypeLayout.visibility = View.VISIBLE
            }
        }

        trafficBtn.setOnClickListener {
            mMap?.isTrafficEnabled = !mMap?.isTrafficEnabled!!

            if (mMap?.isTrafficEnabled!!) {
                ViewCompat.setBackgroundTintList(
                    trafficBtn,
                    ContextCompat.getColorStateList(this@DeviceMapCluster, R.color.colorPrimaryDark)
                )
            } else {
                ViewCompat.setBackgroundTintList(
                    trafficBtn,
                    ContextCompat.getColorStateList(this@DeviceMapCluster, R.color.btn_color)
                )
            }
        }

        mapNormal.setOnClickListener {
            ViewCompat.setBackgroundTintList(
                mapNormal,
                ContextCompat.getColorStateList(this@DeviceMapCluster, R.color.colorPrimaryDark)
            )
            ViewCompat.setBackgroundTintList(
                mapHybrid,
                ContextCompat.getColorStateList(this@DeviceMapCluster, R.color.white)
            )
            ViewCompat.setBackgroundTintList(
                mapSatellite,
                ContextCompat.getColorStateList(this@DeviceMapCluster, R.color.white)
            )
            ViewCompat.setBackgroundTintList(
                mapTerrain,
                ContextCompat.getColorStateList(this@DeviceMapCluster, R.color.white)
            )

            mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            mapTypeLayout.visibility = View.GONE
            localDB.saveMapType("MAP_TYPE_NORMAL")
        }

        mapHybrid.setOnClickListener {
            ViewCompat.setBackgroundTintList(
                mapNormal,
                ContextCompat.getColorStateList(this@DeviceMapCluster, R.color.white)
            )
            ViewCompat.setBackgroundTintList(
                mapHybrid,
                ContextCompat.getColorStateList(this@DeviceMapCluster, R.color.colorPrimaryDark)
            )
            ViewCompat.setBackgroundTintList(
                mapSatellite,
                ContextCompat.getColorStateList(this@DeviceMapCluster, R.color.white)
            )
            ViewCompat.setBackgroundTintList(
                mapTerrain,
                ContextCompat.getColorStateList(this@DeviceMapCluster, R.color.white)
            )
            mMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
            mapTypeLayout.visibility = View.GONE
            localDB.saveMapType("MAP_TYPE_HYBRID")
        }

        mapSatellite.setOnClickListener {
            ViewCompat.setBackgroundTintList(
                mapNormal,
                ContextCompat.getColorStateList(this@DeviceMapCluster, R.color.white)
            )
            ViewCompat.setBackgroundTintList(
                mapHybrid,
                ContextCompat.getColorStateList(this@DeviceMapCluster, R.color.white)
            )
            ViewCompat.setBackgroundTintList(
                mapSatellite,
                ContextCompat.getColorStateList(this@DeviceMapCluster, R.color.colorPrimaryDark)
            )
            ViewCompat.setBackgroundTintList(
                mapTerrain,
                ContextCompat.getColorStateList(this@DeviceMapCluster, R.color.white)
            )
            mMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            mapTypeLayout.visibility = View.GONE
            localDB.saveMapType("MAP_TYPE_SATELLITE")
        }

        mapTerrain.setOnClickListener {
            ViewCompat.setBackgroundTintList(
                mapNormal,
                ContextCompat.getColorStateList(this@DeviceMapCluster, R.color.white)
            )
            ViewCompat.setBackgroundTintList(
                mapHybrid,
                ContextCompat.getColorStateList(this@DeviceMapCluster, R.color.white)
            )
            ViewCompat.setBackgroundTintList(
                mapSatellite,
                ContextCompat.getColorStateList(this@DeviceMapCluster, R.color.white)
            )
            ViewCompat.setBackgroundTintList(
                mapTerrain,
                ContextCompat.getColorStateList(this@DeviceMapCluster, R.color.colorPrimaryDark)
            )
            mMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
            mapTypeLayout.visibility = View.GONE
            localDB.saveMapType("MAP_TYPE_TERRAIN")
        }
    }

    private fun populateClusters(type: String) {
        var lat = 0.0
        var lng = 0.0
        val devices = app?.getDeviceList()
        devices?.forEach { deviceData ->
            deviceData.items?.forEachIndexed { index, items ->
                when {

                    items.icon_color.equals(type, true) && type != "all" -> {
                        lat = items.lat ?: 0.0
                        lng = items.lng ?: 0.0
                        val mapClusterItem = MapClusterItem(
                            lat,
                            lng,
                            items.name ?: "",
                            items.stop_duration!!
                        )
                        mapClusterItem.setColor(items.icon_color!!)
                        mapClusterItem.setItem(items)
                        mClusterManager?.addItem(mapClusterItem)
                    }

                    type == "all" -> {
                        lat = items.lat ?: 0.0
                        lng = items.lng ?: 0.0
                        val mapClusterItem = MapClusterItem(
                            lat,
                            lng,
                            items.name!!,
                            items.stop_duration!!
                        )
                        mapClusterItem.setColor(items.icon_color!!)
                        mapClusterItem.setItem(items)
                        mClusterManager?.addItem(mapClusterItem)
                    }
                }
            }
        }

        mClusterManager?.cluster()
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), currentZoomLevel))

        mMap?.setOnMapClickListener {
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 1f))
        }
    }

    override fun onClusterClick(cluster: Cluster<MapClusterItem>?): Boolean {
        val builder = LatLngBounds.builder()

        val markerItems = cluster?.items

        for (item in markerItems!!) {
            val markerPosition = item.position
            builder.include(markerPosition)
        }

        val bounds = builder.build()

        try {
            mMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        } catch (error: Exception) {
            error.printStackTrace()
        }

        mClusterManager?.cluster()

        return true
    }

    override fun onClusterItemInfoWindowClick(mapItem: MapClusterItem?) {
        Intent(this@DeviceMapCluster, MapPage::class.java).apply {
            putExtra("deviceItem", mapItem?.getItem())
            startActivity(this)
        }
    }

    override fun onClusterItemClick(clusterItem: MapClusterItem?): Boolean {

        mMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                clusterItem?.position, floor(
                    mMap?.cameraPosition!!.zoom + 1
                )
            ), 300,
            null
        )
        return false

        /* mClusterManager?.cluster()
         val render = mClusterManager?.renderer as DeviceMarkerRender
         val marker = render?.getMarkerItem(clusterItem as Cluster<MapClusterItem>)

         if (marker?.isInfoWindowShown!!) {
             Intent(this@DeviceMapCluster, MapPage::class.java).apply {
                 putExtra("deviceItem", clusterItem?.getItem())
                 startActivity(this)
             }

             return true
         } else {

         }*/
    }

    override fun onCameraMove() {
        mClusterManager?.cluster()
        val render = mClusterManager?.renderer as DeviceMarkerRender
        render.clusterMarkerMap.clear()
    }
}