package com.shazcom.gps.app.ui.activities

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.maps.android.ui.IconGenerator
import com.shazcom.gps.app.GPSWoxApp
import com.shazcom.gps.app.R
import com.shazcom.gps.app.back.DeviceService
import com.shazcom.gps.app.back.DeviceServiceConstants
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.CommonViewRepository
import com.shazcom.gps.app.data.response.*
import com.shazcom.gps.app.data.vo.LatLngModel
import com.shazcom.gps.app.databinding.ActivityMapBinding
import com.shazcom.gps.app.ui.BaseActivity
import com.shazcom.gps.app.ui.viewmodal.CommonViewModel
import com.shazcom.gps.app.utils.LocaleHelper
import com.shazcom.gps.app.utils.getCar
import com.shazcom.gps.app.utils.getMapTypes
import com.shazcom.gps.app.utils.isWhite

import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.net.URL
import kotlin.math.roundToInt


class MapPage : BaseActivity(), OnMapReadyCallback, KodeinAware {

    private lateinit var binding: ActivityMapBinding
    private var deviceService: Messenger? = null
    var deviceItem: Items? = null
    var behavior: BottomSheetBehavior<*>? = null
    var mMap: GoogleMap? = null

    override val kodein by kodein()
    private val localDB: LocalDB by instance()
    private val repository: CommonViewRepository by instance<CommonViewRepository>()
    private var commonViewModel: CommonViewModel? = null
    private var currentZoomLevel = 16f

    private var odometerValue = "0.0"
    private var engineLoadValue = "0.0"

    private var lastLat: Double? = 0.0
    private var lastLng: Double? = 0.0

    private var isBound = false

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_map)
       // setContentView(binding.root)

        deviceItem = intent?.extras?.get("deviceItem") as Items?
        with(binding) {
            toolBar.title = deviceItem?.name
            toolBar.subtitle = deviceItem?.time

            commonViewModel = ViewModelProvider(this@MapPage).get(CommonViewModel::class.java)
            commonViewModel?.commonViewRepository = repository

            initBottomSheet()

            deviceItem?.let {
                upadateUI(it)
            }

            toolBar.setNavigationOnClickListener { finish() }

            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync(this@MapPage)

            initClicks()
        }
    }


    private fun inflateSensorView(type: String, value: String) {
        val inflater = LayoutInflater.from(this@MapPage)
        val view = inflater.inflate(R.layout.item_sensors, binding.inc.sensorParent, false)
        val sensorName = view.findViewById<TextView>(R.id.sensorName)
        val sensorValue = view.findViewById<TextView>(R.id.sensorValue)
        sensorName.text = "$type"?.capitalize()
        sensorValue.text = HtmlCompat.fromHtml("$value", HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.inc.sensorLayout.addView(view)
    }


    override fun onDestroy() {
        super.onDestroy()
        val app = application as GPSWoxApp
        app.appIsRunning = false
    }

    @SuppressLint("SetTextI18n", "WrongConstant")
    private fun upadateUI(item: Items) {

        binding.speedTool.text = "${item.speed} ${item.distance_unit_hour}"

        if (binding.inc.sensorLayout.childCount > 0) {
            binding.inc.sensorLayout.removeAllViews()
        }

        inflateSensorView(getString(R.string.speed), "${item.speed} ${item.distance_unit_hour}")
        inflateSensorView(
            getString(R.string.distance),
            "${item.total_distance} ${item.unit_of_distance}"
        )

        item.sensors?.forEach { sensor ->
            inflateSensorView(
                sensor.type.toString(),
                sensor.value.toString()
            )
        }

        if (item.lat != null && item.lng != null) {
            if (checkNewLocation(item)) {
                val location = Location(deviceItem?.name)
                location.latitude = item.lat
                location.longitude = item.lng
                startIntentService(location)
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            delay(500)
            binding.inc.mapDataSheet.post {
                loadTail()
            }
        }
    }


    private fun checkNewLocation(item: Items): Boolean {
        Log.e("previous Location", "$lastLat $lastLng")
        Log.e("Next Location", "${item.lat} ${item.lng}")
        if (item.lat != lastLat || item.lng != lastLng) {
            Log.e("Update Location", "Updating....")
            return true
        }

        Log.e("Update Location", "No need to call the api")
        return false
    }


    private fun checkNewLocationLatest(item: LatestItem): Boolean {
        Log.e("previous Location", "$lastLat $lastLng")
        Log.e("Next Location", "${item.lat} ${item.lng}")
        if (item.lat != lastLat || item.lng != lastLng) {
            Log.e("Update Location", "Updating....")
            return true
        }

        Log.e("Update Location", "No need to call the api")
        return false
    }

    private fun initBottomSheet() {

        if (behavior != null) {
            return
        }

        behavior = BottomSheetBehavior.from(binding.inc.mapDataSheet)
        behavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        behavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

                val h = bottomSheet.height.toFloat()
                val off = h * slideOffset

                when (behavior?.state) {
                    BottomSheetBehavior.STATE_SETTLING -> {
                        bottomSheet.requestLayout()
                        setMapPaddingBottom(off)
                    }

                    BottomSheetBehavior.STATE_COLLAPSED -> {

                    }

                    BottomSheetBehavior.STATE_DRAGGING -> {

                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {

                    }

                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {

                    }

                    BottomSheetBehavior.STATE_HIDDEN -> {

                    }
                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }
        })

        binding.inc.mapDataSheet.findViewById<ImageView>(R.id.osViewBtn).setOnClickListener {
            try {
                val gmmIntentUri: Uri =
                    Uri.parse("google.streetview:cbll=${deviceItem?.lat},${deviceItem?.lng}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            } catch (ex: Exception) {
                Toast.makeText(
                    this@MapPage,
                    getString(R.string.google_map_not_available),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    override fun popUpAddress(addressOutput: String) {
        CoroutineScope(Dispatchers.Main).launch {
            binding.inc.address.text = "$addressOutput"
        }
    }

    private fun setMapPaddingBottom(off: Float) {
        val maxMapPaddingBottom = 1.0f
        mMap?.setPadding(0, 0, 0, (off * maxMapPaddingBottom).roundToInt())

 val carLocation = LatLng(deviceItem?.lat!!, deviceItem?.lng!!)
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(carLocation, currentZoomLevel))
    }

    override fun onMapReady(googleMap: GoogleMap?) {

        googleMap?.mapType = getMapTypes(localDB.getMapType()!!)
        selectedMap(getMapTypes(localDB.getMapType()!!))
        googleMap?.uiSettings?.isMapToolbarEnabled = true
        googleMap?.uiSettings?.setAllGesturesEnabled(true)
        googleMap?.uiSettings?.isTiltGesturesEnabled = true
        googleMap?.uiSettings?.isRotateGesturesEnabled = true

        mMap = googleMap

        mMap?.setOnMapClickListener {
            if (behavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
                behavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
            } else {
                behavior?.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            delay(1000)
            if (behavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
                behavior?.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    private fun loadTail() {


        mMap?.clear()
        mMap?.mapType = getMapTypes(localDB.getMapType() ?: "MAP_TYPE_NORMAL")

        val listLatlng = arrayListOf<LatLng>()
        deviceItem?.tail?.forEach { tail ->
            listLatlng.add(LatLng(tail.lat!!, tail.lng!!))
        }

        var colorPrimary = ContextCompat.getColor(this@MapPage, android.R.color.holo_green_dark)
        if (mMap?.mapType == GoogleMap.MAP_TYPE_HYBRID || mMap?.mapType == GoogleMap.MAP_TYPE_SATELLITE) {
            colorPrimary = ContextCompat.getColor(this@MapPage, android.R.color.holo_red_dark)
        }

        if (localDB.isTailFlag()) {
            renderMarker(R.color.colorPrimary, binding.mapTail)
            mMap?.addPolyline(
                PolylineOptions().color(colorPrimary).width(12f).geodesic(true).addAll(listLatlng)
            )
        } else {
            renderMarker(R.color.white, binding.mapTail)
        }

        var carLocation = LatLng(deviceItem?.lat!!, deviceItem?.lng!!)
        lastLat = deviceItem?.lat!!
        lastLng = deviceItem?.lng!!

        var bearer: Float?
        var bearerLocation: LatLng? = null

        val loc1 = Location("oldPos")
        val loc2 = Location("newPos")

        if (listLatlng.size > 0) {

            bearerLocation = listLatlng[listLatlng.size - 1]

            var bearerLocation1: LatLng? = null
            if (listLatlng.size > 1) {
                bearerLocation1 = listLatlng[listLatlng.size - 2]
            }

            if (bearerLocation1 != null) {
                loc1.latitude = bearerLocation1.latitude
                loc1.longitude = bearerLocation1.longitude
            } else {
                loc1.latitude = carLocation.latitude
                loc1.longitude = carLocation.longitude
            }

            loc2.latitude = bearerLocation.latitude
            loc2.longitude = bearerLocation.longitude
            bearer = loc1.bearingTo(loc2)
        } else {
            bearer = 0f
        }

        if (bearerLocation != null) {
            carLocation = bearerLocation
        }

        mMap?.addMarker(
            MarkerOptions().position(carLocation)
                .flat(true)
                .title(deviceItem?.name!!)
                .rotation(bearer)
                .icon(
                    BitmapDescriptorFactory.fromResource(getCar(deviceItem?.icon_color!!))
                )
        )

        mMap?.moveCamera(CameraUpdateFactory.newLatLng(carLocation))
        mMap?.animateCamera(CameraUpdateFactory.zoomTo(currentZoomLevel))

        if (localDB.isGeofenceFlag()) {
            renderGeofence()
        }

        if (localDB.isPOIFlag()) {
            renderPoi()
        }
    }

    private fun loadTailLatest(item: LatestItem) {


        mMap?.clear()
        mMap?.mapType = getMapTypes(localDB.getMapType() ?: "MAP_TYPE_NORMAL")

        val listLatlng = arrayListOf<LatLng>()


        try {
            val listType = object : TypeToken<List<Tail?>?>() {}.type
            val tailData: List<Tail> = Gson().fromJson(item.tail, listType)
            tailData.forEach { tail ->
                listLatlng.add(LatLng(tail.lat!!, tail.lng!!))
            }
        } catch (ex: JSONException) {
            ex.printStackTrace()
        }


        var colorPrimary = ContextCompat.getColor(this@MapPage, android.R.color.holo_green_dark)
        if (mMap?.mapType == GoogleMap.MAP_TYPE_HYBRID || mMap?.mapType == GoogleMap.MAP_TYPE_SATELLITE) {
            colorPrimary = ContextCompat.getColor(this@MapPage, android.R.color.holo_red_dark)
        }

        if (localDB.isTailFlag()) {
            renderMarker(R.color.colorPrimary, binding.mapTail)
            mMap?.addPolyline(
                PolylineOptions().color(colorPrimary).width(12f).geodesic(true).addAll(listLatlng)
            )
        } else {
            renderMarker(R.color.white, binding.mapTail)
        }

        var carLocation = LatLng(item.lat, item.lng)
        lastLat = item.lat
        lastLng = item.lng

        var bearer: Float?
        var bearerLocation: LatLng? = null

        val loc1 = Location("oldPos")
        val loc2 = Location("newPos")

        if (listLatlng.size > 0) {

            bearerLocation = listLatlng[listLatlng.size - 1]

            var bearerLocation1: LatLng? = null
            if (listLatlng.size > 1) {
                bearerLocation1 = listLatlng[listLatlng.size - 2]
            }

            if (bearerLocation1 != null) {
                loc1.latitude = bearerLocation1.latitude
                loc1.longitude = bearerLocation1.longitude
            } else {
                loc1.latitude = carLocation.latitude
                loc1.longitude = carLocation.longitude
            }

            loc2.latitude = bearerLocation.latitude
            loc2.longitude = bearerLocation.longitude
            bearer = loc1.bearingTo(loc2)
        } else {
            bearer = 0f
        }

        if (bearerLocation != null) {
            carLocation = bearerLocation
        }

        mMap?.addMarker(
            MarkerOptions().position(carLocation)
                .flat(true)
                .title(item?.name)
                .rotation(bearer)
                .icon(
                    BitmapDescriptorFactory.fromResource(getCar(item.icon_color))
                )
        )

        mMap?.moveCamera(CameraUpdateFactory.newLatLng(carLocation))
        mMap?.animateCamera(CameraUpdateFactory.zoomTo(currentZoomLevel))

        if (localDB.isGeofenceFlag()) {
            renderGeofence()
        }

        if (localDB.isPOIFlag()) {
            renderPoi()
        }
    }

    private fun processData(data: LatestDeviceData) {
        data.let { latestData ->
            val result = latestData.items.find { it.id == deviceItem?.id }
            result?.let {
                upadateLatestUI(it)
            }
        }
    }

    private fun upadateLatestUI(item: LatestItem) {


        binding.speedTool.text = "${item.speed} ${item.distance_unit_hour}"
        binding. toolBar.subtitle = item?.time

        if (binding.inc.sensorLayout.childCount > 0) {
            binding.inc.sensorLayout.removeAllViews()
        }

        inflateSensorView(getString(R.string.speed), "${item.speed} ${item.distance_unit_hour}")
        inflateSensorView(
            getString(R.string.distance),
            "${item.total_distance} ${item.unit_of_distance}"
        )

        try {
            val listType = object : TypeToken<List<SensorMain?>?>() {}.type
            val sensorData: List<SensorMain> = Gson().fromJson(item.sensors, listType)
            sensorData.forEach { sensor ->
                inflateSensorView(
                    sensor.type.toString(),
                    sensor.value.toString()
                )
            }
        } catch (ex: JSONException) {
            ex.printStackTrace()
        }

        if (item.lat != null && item.lng != null) {
            if (checkNewLocationLatest(item)) {
                val location = Location(deviceItem?.name)
                location.latitude = item.lat
                location.longitude = item.lng
                startIntentService(location)
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            delay(500)
            binding.inc.mapDataSheet.post {
                loadTailLatest(item)
            }
        }

        Log.e("test time ", "${System.currentTimeMillis()}")
    }

    private fun initClicks() {

        binding.inc. address.setOnClickListener {
            if (it.isPressed) {
                if (behavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
                    behavior?.setState(BottomSheetBehavior.STATE_EXPANDED)

                } else {
                    behavior?.setState(BottomSheetBehavior.STATE_COLLAPSED)
                }
            }
        }

        binding.inc.incPlay.summaryBtn.setOnClickListener {
            Intent(this@MapPage, SummaryPage::class.java).apply {
                putExtra("deviceId", deviceItem?.id!!)
                putExtra("deviceName", deviceItem?.name!!)
                startActivity(this)
            }
        }
        binding.inc.incPlay.playBtn.setOnClickListener {
            Intent(this@MapPage, PlayBackRoute::class.java).apply {
                putExtra("deviceId", deviceItem?.id!!)
                putExtra("deviceName", deviceItem?.name!!)
                startActivity(this)
            }
        }

        binding.inc.incPlay.routeBtn.setOnClickListener {
            Intent(this@MapPage, RoutePage::class.java).apply {
                putExtra("deviceId", deviceItem?.id!!)
                putExtra("deviceName", deviceItem?.name!!)
                startActivity(this)
            }
        }

        binding.inc.incPlay.eventsBtn.setOnClickListener {
            Intent(this@MapPage, EventPage::class.java).apply {
                putExtra("deviceId", deviceItem?.id!!)
                putExtra("deviceName", deviceItem?.name!!)
                startActivity(this)
            }
        }

        binding.inc.incPlay.serviceBtn.setOnClickListener {
             Intent(this@MapPage, ServicePage::class.java).apply {
                  putExtra("deviceId", deviceItem?.id!!)
                  putExtra("deviceName", deviceItem?.name!!)
                  putExtra("odometer", odometerValue)
                  putExtra("engineLoad", engineLoadValue)
                  startActivity(this)
              }

          /*  Intent(this@MapPage,MoreActivity::class.java).apply {
                putExtra("item", deviceItem)
                startActivity(this)
            }*/
        }

        binding.  zoomIn.setOnClickListener {
            mMap?.animateCamera(CameraUpdateFactory.zoomIn())
            if (currentZoomLevel < mMap?.maxZoomLevel!!) {
                currentZoomLevel += 1
            }
        }
        binding.zoomOut.setOnClickListener {
            mMap?.animateCamera(CameraUpdateFactory.zoomOut())
            if (currentZoomLevel > mMap?.minZoomLevel!!) {
                currentZoomLevel -= 1
            }
        }

        binding. mapType.setOnClickListener {
            if (binding.mapTypeLayout.isVisible) {
                binding.mapTypeLayout.visibility = View.GONE
                binding. mapFeatureLayout.visibility = View.GONE
            } else {
                binding.mapTypeLayout.visibility = View.VISIBLE
                binding. mapFeatureLayout.visibility = View.VISIBLE
            }
        }

        binding.trafficBtn.setOnClickListener {
            mMap?.isTrafficEnabled = !mMap?.isTrafficEnabled!!

            if (mMap?.isTrafficEnabled!!) {
                ViewCompat.setBackgroundTintList(
                    binding.trafficBtn,
                    ContextCompat.getColorStateList(this@MapPage, R.color.colorPrimaryDark)
                )
            } else {
                ViewCompat.setBackgroundTintList(
                    binding.trafficBtn,
                    ContextCompat.getColorStateList(this@MapPage, R.color.btn_color)
                )
            }
        }

        binding. mapNormal.setOnClickListener {
            mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            binding.mapTypeLayout.visibility = View.GONE
            binding.mapFeatureLayout.visibility = View.GONE
            localDB.saveMapType("MAP_TYPE_NORMAL")
            selectedMap(1)
        }

        binding.  mapHybrid.setOnClickListener {

            mMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
            binding. mapTypeLayout.visibility = View.GONE
            binding.  mapFeatureLayout.visibility = View.GONE
            localDB.saveMapType("MAP_TYPE_HYBRID")
            selectedMap(4)
        }

        binding. mapSatellite.setOnClickListener {

            mMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            binding.mapTypeLayout.visibility = View.GONE
            binding. mapFeatureLayout.visibility = View.GONE
            localDB.saveMapType("MAP_TYPE_SATELLITE")
            selectedMap(2)
        }

        binding. mapTerrain.setOnClickListener {

            mMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
            binding.mapTypeLayout.visibility = View.GONE
            binding.mapFeatureLayout.visibility = View.GONE
            localDB.saveMapType("MAP_TYPE_TERRAIN")
            selectedMap(3)
        }

        binding.  mapTail.setOnClickListener {
            localDB?.flipTailFlag()
            renderTail()
            binding.mapTypeLayout.visibility = View.GONE
            binding.mapFeatureLayout.visibility = View.GONE
        }

        binding.  mapPoi.setOnClickListener {
            localDB?.flipPOIFlag()
            renderPoi()
            binding.mapTypeLayout.visibility = View.GONE
            binding. mapFeatureLayout.visibility = View.GONE

        }

        binding.  mapGeoFence.setOnClickListener {
            localDB?.flipGeofenceFlag()
            renderGeofence()
            binding.mapTypeLayout.visibility = View.GONE
            binding. mapFeatureLayout.visibility = View.GONE
        }
    }

    private fun renderPoi() {
        val poiMarkers = (application as GPSWoxApp).getPoiMarkers()
        poiMarkers?.forEach { poiMarker ->
            drawPoiMarker(poiMarker)
        }

        if (localDB.isPOIFlag()) {
            renderMarker(R.color.colorPrimary,binding. mapPoi)
        } else {
            renderMarker(R.color.white, binding.mapPoi)
        }
    }

    private fun renderTail() {
        loadTail()
        binding.mapFeatureLayout.visibility = View.GONE
        binding.mapTypeLayout.visibility = View.GONE

        if (localDB.isTailFlag()) {
            renderMarker(R.color.colorPrimary, binding.mapTail)
        } else {
            renderMarker(R.color.white, binding.mapTail)
        }
    }

    private fun renderGeofence() {
        val geofenceList = (application as GPSWoxApp).getGeoFence()
        geofenceList?.forEach { geofence ->
            drawPolygon(geofence)
        }

        if (localDB.isGeofenceFlag()) {
            renderMarker(R.color.colorPrimary, binding.mapGeoFence)
        } else {
            renderMarker(R.color.white, binding.mapGeoFence)
        }
    }

    private fun drawPolygon(geoFenceData: GeoFenceData) {

        CoroutineScope(Dispatchers.Default).launch {
            if (geoFenceData.coordinates.isNotEmpty()) {
                val jsonString = JSONArray(geoFenceData.coordinates).toString()
                val listCoordinates =
                    Gson().fromJson(jsonString, Array<LatLngModel>::class.java).toList()

                val latlngList = ArrayList<LatLng>()
                listCoordinates?.forEach {
                    latlngList.add(LatLng(it.lat!!, it.lng!!))
                }

                withContext(Dispatchers.Main) {

                    val iconGenerator = IconGenerator(this@MapPage)
                    iconGenerator.setColor(Color.parseColor(geoFenceData.polygon_color))
                    iconGenerator.setTextAppearance(isWhite(geoFenceData.polygon_color))
                    val bm = iconGenerator.makeIcon("${geoFenceData.name}")

                    mMap?.addPolygon(
                        PolygonOptions()
                            .fillColor(Color.parseColor(geoFenceData.polygon_color))
                            .clickable(false)
                            .addAll(latlngList)
                    )

                    mMap?.addMarker(
                        MarkerOptions().position(latlngList[0])
                            .icon(BitmapDescriptorFactory.fromBitmap(bm))
                    )

                }

            }
        }
    }

    private fun drawPoiMarker(poiMarker: MapIcons) {
        val jsonString = JSONObject(poiMarker.coordinates.replace("\\", "")).toString()
        val latLngBounds: LatLngModel = Gson().fromJson(jsonString, LatLngModel::class.java)
        val poiLocation = LatLng(latLngBounds.lat!!, latLngBounds.lng!!)
        var bitmap: Bitmap? = null

        try {
            CoroutineScope(Dispatchers.IO).launch {
                val url = URL(poiMarker.map_icon.url)
                bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                val smallMarker = Bitmap.createScaledBitmap(bitmap!!, 120, 120, false)
                withContext(Dispatchers.Main) {
                    bitmap?.let {
                        mMap?.addMarker(
                            MarkerOptions().position(poiLocation)
                                .flat(true)
                                .title(poiMarker.name)
                                .snippet(poiMarker.description)
                                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                        )
                    }
                }
            }
        } catch (e: Exception) {
            println(e)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleHelper.onAttach(this)
    }

    private fun selectedMap(mapType: Int) {
        when (mapType) {

            1 -> {


                ViewCompat.setBackgroundTintList(
                    binding.mapHybrid,
                    ContextCompat.getColorStateList(this@MapPage, R.color.white)
                )
                ViewCompat.setBackgroundTintList(
                    binding.mapSatellite,
                    ContextCompat.getColorStateList(this@MapPage, R.color.white)
                )
                ViewCompat.setBackgroundTintList(
                    binding.mapTerrain,
                    ContextCompat.getColorStateList(this@MapPage, R.color.white)
                )
            }

            2 -> {
                ViewCompat.setBackgroundTintList(
                    binding.mapNormal,
                    ContextCompat.getColorStateList(this@MapPage, R.color.white)
                )
                ViewCompat.setBackgroundTintList(
                    binding.mapHybrid,
                    ContextCompat.getColorStateList(this@MapPage, R.color.white)
                )
                ViewCompat.setBackgroundTintList(
                    binding.mapSatellite,
                    ContextCompat.getColorStateList(this@MapPage, R.color.colorPrimaryDark)
                )
                ViewCompat.setBackgroundTintList(
                    binding.mapTerrain,
                    ContextCompat.getColorStateList(this@MapPage, R.color.white)
                )
            }

            3 -> {
                ViewCompat.setBackgroundTintList(
                    binding. mapNormal,
                    ContextCompat.getColorStateList(this@MapPage, R.color.white)
                )
                ViewCompat.setBackgroundTintList(
                    binding.mapHybrid,
                    ContextCompat.getColorStateList(this@MapPage, R.color.white)
                )
                ViewCompat.setBackgroundTintList(
                    binding.mapSatellite,
                    ContextCompat.getColorStateList(this@MapPage, R.color.white)
                )
                ViewCompat.setBackgroundTintList(
                    binding.mapTerrain,
                    ContextCompat.getColorStateList(this@MapPage, R.color.colorPrimaryDark)
                )
            }

            4 -> {
                ViewCompat.setBackgroundTintList(
                    binding.mapNormal,
                    ContextCompat.getColorStateList(this@MapPage, R.color.white)
                )
                ViewCompat.setBackgroundTintList(
                    binding.mapHybrid,
                    ContextCompat.getColorStateList(this@MapPage, R.color.colorPrimaryDark)
                )
                ViewCompat.setBackgroundTintList(
                    binding.mapSatellite,
                    ContextCompat.getColorStateList(this@MapPage, R.color.white)
                )
                ViewCompat.setBackgroundTintList(
                    binding.mapTerrain,
                    ContextCompat.getColorStateList(this@MapPage, R.color.white)
                )
            }

        }
    }

    private fun renderMarker(color: Int, btnView: View) {
        ViewCompat.setBackgroundTintList(
            btnView,
            ContextCompat.getColorStateList(this@MapPage, color)
        )
    }


    override fun onStart() {
        super.onStart()

        val intent = Intent(this, DeviceService::class.java)
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE)

        val app = application as GPSWoxApp
        app.appIsRunning = true
    }

    override fun onStop() {
        super.onStop()

        if (isBound) {
            unbindService(mConnection)
            isBound = false;
        }

        val app = application as GPSWoxApp
        app.appIsRunning = false
    }


    var replyMessenger: Messenger = Messenger(HandlerReplyMsg())
    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            deviceService = Messenger(service)
            isBound = true

            val msg = Message.obtain(null, DeviceServiceConstants.ACTION_CONNECT_APP, 0, 0)
            msg.replyTo = replyMessenger

            try {
                deviceService?.send(msg)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            isBound = false
        }
    }


    inner class HandlerReplyMsg : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            Log.i("DeviceService", "Receiving Data")
            val latestData = msg.obj as LatestDeviceData
            processData(latestData)
        }
    }

}