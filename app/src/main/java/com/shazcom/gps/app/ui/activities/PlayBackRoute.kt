package com.shazcom.gps.app.ui.activities

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.gzuliyujiang.wheelpicker.DatimePicker
import com.github.gzuliyujiang.wheelpicker.annotation.DateMode
import com.github.gzuliyujiang.wheelpicker.annotation.TimeMode
import com.github.gzuliyujiang.wheelpicker.contract.DateFormatter
import com.github.gzuliyujiang.wheelpicker.entity.DatimeEntity
import com.github.gzuliyujiang.wheelpicker.widget.DatimeWheelLayout
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.*
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.CommonViewRepository
import com.shazcom.gps.app.data.response.HistoryData
import com.shazcom.gps.app.data.response.ItemMain
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.BaseActivity
import com.shazcom.gps.app.ui.viewmodal.CommonViewModel
import com.shazcom.gps.app.utils.getCurrentDay
import com.shazcom.gps.app.utils.getMapTypes
import com.shazcom.gps.app.utils.getNewTimeFormat
import com.shazcom.gps.app.utils.nextDay
import kotlinx.android.synthetic.main.activity_playback.*
import kotlinx.android.synthetic.main.bottom_sheet_buttons.progressBar
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.text.DecimalFormat
import java.util.*


class PlayBackRoute : BaseActivity(), KodeinAware, AdapterView.OnItemSelectedListener,
    OnMapReadyCallback, GoogleMap.OnCameraMoveListener {


    private var distanceSum: Double? = 0.0
    private var playPauseFlag: Boolean = false
    private var playBackSpeed: Long = 1800L
    private var marker: Marker? = null
    private var itemPos = 0
    private var totalPoints = 0
    var handler = Handler()
    private var currentZoom = 16f

    val df = DecimalFormat("#.##")

    private val playSpeedItems = listOf("1x", "2x", "3x", "4x", "5x", "6x")
    private val routeList = arrayListOf<LatLng>()
    private val itemList = arrayListOf<ItemMain>()

    override val kodein by kodein()
    private val localDB: LocalDB by instance<LocalDB>()
    private val repository: CommonViewRepository by instance<CommonViewRepository>()
    private var commonViewModel: CommonViewModel? = null
    private var mMap: GoogleMap? = null

    private var deviceId: Int? = null
    private var deviceName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playback)

        deviceId = intent.getIntExtra("deviceId", 0)
        deviceName = intent.getStringExtra("deviceName")

        toolBar.title = deviceName!!

        startDateTime.text = "${getCurrentDay()}"
        endDateTime.text = "${nextDay()}"

        commonViewModel = ViewModelProvider(this).get(CommonViewModel::class.java)
        commonViewModel?.commonViewRepository = repository

        val spinnerAdapter: ArrayAdapter<*> =
            ArrayAdapter<Any?>(this, android.R.layout.simple_spinner_item, playSpeedItems)
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item)
        playSpeedDrop.onItemSelectedListener = this
        playSpeedDrop.adapter = spinnerAdapter


        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)


        toolBar.setNavigationOnClickListener { finish() }

        playSpeedBtn.setOnClickListener {
            playSpeedDrop.performClick()
        }

        playBtn.setOnClickListener {
            if (!playPauseFlag) {
                playPauseFlag = true
                playBtn.setImageResource(R.drawable.pause)
                playRoute()
            } else {
                playBtn.setImageResource(R.drawable.playback)
                playPauseFlag = false
            }
        }

        startDateTime.setOnClickListener {
            if (startDateTime.isPressed) {
                pickStartDate()
            }
        }

        endDateTime.setOnClickListener {
            if (endDateTime.isPressed) {
                pickEndDate()
            }
        }

        routeBtn.setOnClickListener {
            loadHistory()
        }


        mapType.setOnClickListener {
            if (mapTypeLayout.isVisible) {
                mapTypeLayout.visibility = View.GONE
            } else {
                mapTypeLayout.visibility = View.VISIBLE
            }
        }

        initClicks()
        loadHistory()

    }

    private fun initClicks() {

        mapNormal.setOnClickListener {
            mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            mapTypeLayout.visibility = View.GONE
            localDB.saveMapType("MAP_TYPE_NORMAL")
            selectedMap(1)
        }

        mapHybrid.setOnClickListener {

            mMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
            mapTypeLayout.visibility = View.GONE
            localDB.saveMapType("MAP_TYPE_HYBRID")
            selectedMap(4)
        }

        mapSatellite.setOnClickListener {

            mMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            mapTypeLayout.visibility = View.GONE
            localDB.saveMapType("MAP_TYPE_SATELLITE")
            selectedMap(2)
        }

        mapTerrain.setOnClickListener {

            mMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
            mapTypeLayout.visibility = View.GONE
            localDB.saveMapType("MAP_TYPE_TERRAIN")
            selectedMap(3)
        }
    }

    private fun loadHistory() {

        itemPos = 0
        playPauseFlag = false
        marker = null
        speedLayout.visibility = View.INVISIBLE
        playBtn.setImageResource(R.drawable.playback)
        playBackSpeed = 1500L

        val fromDate = startDateTime.text.split("\n")[0]
        val fromTime = startDateTime.text.split("\n")[1]

        val toDate = endDateTime.text.split("\n")[0]
        val toTime = endDateTime.text.split("\n")[1]

        commonViewModel?.getHistoryInfo(
            "en",
            localDB.getToken()!!,
            deviceId!!,
            fromDate.replace(" ", ""),
            getNewTimeFormat(fromTime),
            toDate.replace(" ", ""),
            getNewTimeFormat(toTime)
        )
            ?.observe(this, Observer { resources ->
                when (resources.status) {
                    Status.SUCCESS -> {
                        processData(resources.data!!)
                        progressBar.visibility = View.INVISIBLE
                    }

                    Status.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                        itemList.clear()
                        routeList.clear()
                        mMap.let {
                            it?.clear()
                        }
                    }

                    Status.ERROR -> {
                        progressBar.visibility = View.INVISIBLE
                        Toast.makeText(
                            this,
                            getString(R.string.no_playback_Data),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }

    private fun processData(data: HistoryData) {

        if (data.items.isNotEmpty()) {
            data.items.forEach { itemInner ->
                itemInner.items.forEach { routeItem ->
                    if (routeItem.id != null) {
                        itemList.add(routeItem)
                    }

                    if (routeItem.latitude != null || routeItem.longitude != null) {
                        routeList.add(LatLng(routeItem.latitude!!, routeItem.longitude!!))
                    }
                }
            }

            // generate route and move map to route
            if (routeList.size > 0) {
                totalPoints = routeList.size
                mMap?.let {

                    var colorPrimary =
                        ContextCompat.getColor(this@PlayBackRoute, android.R.color.holo_green_dark)
                    if (mMap?.mapType == GoogleMap.MAP_TYPE_HYBRID || mMap?.mapType == GoogleMap.MAP_TYPE_SATELLITE) {
                        colorPrimary = ContextCompat.getColor(
                            this@PlayBackRoute,
                            android.R.color.holo_red_dark
                        )
                    }

                    mMap?.addPolyline(
                        PolylineOptions().color(colorPrimary).geodesic(true).width(7.0f)
                            .addAll(routeList)
                    )


                    mMap?.addMarker(
                        MarkerOptions().position(routeList[0])
                            .icon(
                                BitmapDescriptorFactory.fromResource(R.drawable.map_pin_start)
                            )
                    )

                    mMap?.addMarker(
                        MarkerOptions().position(routeList[routeList.size - 1])
                            .icon(
                                BitmapDescriptorFactory.fromResource(R.drawable.map_pin_end)
                            )
                    )

                    zoomRoute(mMap, routeList)
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.no_playback_Data), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        this.playSpeedBtn.text = playSpeedItems[position]
        when (position) {
            0 -> {
                playBackSpeed = 1500L
            }

            1 -> {
                playBackSpeed = 1300L
            }

            2 -> {
                playBackSpeed = 1100L
            }

            3 -> {
                playBackSpeed = 900L
            }

            4 -> {
                playBackSpeed = 700L
            }

            5 -> {
                playBackSpeed = 500L
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {

        googleMap?.uiSettings?.isMyLocationButtonEnabled = true
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        googleMap?.uiSettings?.isZoomGesturesEnabled = true
        googleMap?.uiSettings?.isCompassEnabled = true
        googleMap?.uiSettings?.isMapToolbarEnabled = true
        googleMap?.uiSettings?.setAllGesturesEnabled(true)
        googleMap?.setOnCameraMoveListener(this)

        googleMap?.mapType = getMapTypes(localDB.getMapType()!!)
        selectedMap(getMapTypes(localDB.getMapType()!!))
        mMap = googleMap
    }

    private fun zoomRoute(
        googleMap: GoogleMap?,
        lstLatLngRoute: List<LatLng?>?
    ) {
        if (googleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return
        val boundsBuilder = LatLngBounds.Builder()
        for (latLngPoint in lstLatLngRoute) boundsBuilder.include(latLngPoint)
        val routePadding = 100
        val latLngBounds = boundsBuilder.build()
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding))
    }

    private fun playRoute() {

        routeList.isNotEmpty().let { flag ->
            if (flag) {

                if (marker === null) {
                    marker = mMap!!.addMarker(
                        MarkerOptions().position(routeList[0])
                            .flat(true)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_default_running))
                    )
                    marker?.position = routeList[0]
                }

                handler.post(object : Runnable {
                    override fun run() {
                        val currentPos = itemPos
                        val newPos = itemPos + 1

                        if (newPos >= routeList.size) {
                            resetPlayer()
                            return
                        }

                        animateCar(routeList[newPos], routeList[currentPos])

                        if (playPauseFlag) {
                            Log.e("PlayBack Speed", "$playBackSpeed")
                            handler.postDelayed(this, playBackSpeed)
                        }

                    }
                })
            }
        }
    }

    private fun resetPlayer() {
        itemPos = 0
        Log.e("resetPlayer", "$itemPos")
        playBtn.setImageResource(R.drawable.playback)
        playPauseFlag = false
    }

    @SuppressLint("SetTextI18n")
    private fun animateCar(newLatLng: LatLng, oldLatLng: LatLng) {

        // update top bar
        if (playPauseFlag) {

            if (itemList[itemPos]?.id == null) {
                return
            }

            itemList[itemPos]?.let {
                //Log.e("Item logged", "${it}")
                speedLayout.visibility = View.VISIBLE
                time.text = itemList[itemPos].time
                speed.text = "${df.format(itemList[itemPos].speed)} km/h"
                distanceSum = distanceSum?.plus(itemList[itemPos].distance!!)
                distance.text = "${df.format(distanceSum)} km"
            }
        }


        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = 300
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.addUpdateListener { valueAnimator ->
            val v = valueAnimator.animatedFraction
            val lng: Double = v * newLatLng.longitude + (1 - v) * oldLatLng.longitude
            val lat: Double = v * newLatLng.latitude + (1 - v) * oldLatLng.latitude
            val newPos = LatLng(lat, lng)
            marker?.position = newPos

            marker?.setAnchor(0.5f, 0.5f)

            val loc1 = Location("oldPos")
            val loc2 = Location("newPos")

            loc1.latitude = oldLatLng.latitude
            loc1.longitude = oldLatLng.longitude

            loc2.latitude = newLatLng.latitude
            loc2.longitude = newLatLng.longitude

            marker?.rotation = loc1.bearingTo(loc2)
            mMap!!.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder().target(newPos)
                        .zoom(currentZoom).build()
                )
            )
        }
        valueAnimator.start()
        itemPos += 1
    }

    override fun onStop() {
        super.onStop()
        if (handler != null) {
            playPauseFlag = false
        }
    }

    private fun pickStartDate() {
       /* val currentDateTime = Calendar.getInstance()
        currentDateTime.add(Calendar.YEAR,-1)
        val dateTimeSelectedListener = object : OnDateTimeSelectedListener {
            override fun onDateTimeSelected(selectedDateTime: Calendar) {
                val df = SimpleDateFormat("yyyy-MM-dd\nhh:mm a")
                startDateTime.text =
                    df.format(selectedDateTime.timeInMillis).replace("PG", "AM")
                        .replace("PTG", "PM")
            }
        }

        val dateTimePickerDialog = DialogDateTimePicker(
            this, //context
            currentDateTime, //start date of calendar
            12, //No. of future months to shown in calendar
            dateTimeSelectedListener,
            "Select start date and time"
        )

        dateTimePickerDialog.show()*/


        val formatter = DecimalFormat("00")
        val picker = DatimePicker(this)
        val wheelLayout: DatimeWheelLayout = picker.getWheelLayout()
        picker.setOnDatimePickedListener { year, month, day, hour, minute, second ->
            var text = "$year-${formatter.format(month)}-${formatter.format(day)}\n${formatter.format(hour)}:${formatter.format(minute)}"
            text += if (wheelLayout.timeWheelLayout.isAnteMeridiem) " AM" else " PM"
            startDateTime.text = text
        }
        wheelLayout.setDateMode(DateMode.YEAR_MONTH_DAY)
        wheelLayout.setTimeMode(TimeMode.HOUR_12_NO_SECOND)
        wheelLayout.setRange(DatimeEntity.yearOnFuture(-5), DatimeEntity.now(), DatimeEntity.now())

        wheelLayout.setDateLabel("Yr", "M", "D")
        wheelLayout.setTimeLabel("hr", "min", "sec")
        picker.show()

    }

    private fun pickEndDate() {
     /*   val currentDateTime = Calendar.getInstance()

        currentDateTime.add(Calendar.YEAR,-1)
        val dateTimeSelectedListener = object : OnDateTimeSelectedListener {
            override fun onDateTimeSelected(selectedDateTime: Calendar) {
                val df = SimpleDateFormat("yyyy-MM-dd\nhh:mm a")
                endDateTime.text =
                    df.format(selectedDateTime.timeInMillis).replace("PG", "AM")
                        .replace("PTG", "PM")
            }
        }

        val dateTimePickerDialog = DialogDateTimePicker(
            this, //context
            currentDateTime, //start date of calendar
            12, // // No. of future months to shown in calendar
            dateTimeSelectedListener,
            "Select end date and time",

        )
        dateTimePickerDialog.show()*/

        val formatter = DecimalFormat("00")

        val picker = DatimePicker(this)
        val wheelLayout: DatimeWheelLayout = picker.getWheelLayout()
        picker.setOnDatimePickedListener { year, month, day, hour, minute, second ->
            var text = "$year-${formatter.format(month)}-${formatter.format(day)}\n${formatter.format(hour)}:${formatter.format(minute)}"
            text += if (wheelLayout.timeWheelLayout.isAnteMeridiem) " AM" else " PM"
            endDateTime.text = text
        }
        wheelLayout.setDateMode(DateMode.YEAR_MONTH_DAY)
        wheelLayout.setTimeMode(TimeMode.HOUR_12_NO_SECOND)
        wheelLayout.setRange(DatimeEntity.yearOnFuture(-5), DatimeEntity.now(), DatimeEntity.now())
        wheelLayout.setDateLabel("Yr", "M", "D")
        wheelLayout.setTimeLabel("hr", "min", "sec")
        picker.show()
    }

    override fun onCameraMove() {
        currentZoom = mMap?.cameraPosition?.zoom!!
    }

    private fun selectedMap(mapType: Int) {
        when (mapType) {

            1 -> {


                ViewCompat.setBackgroundTintList(
                    mapHybrid,
                    ContextCompat.getColorStateList(this@PlayBackRoute, R.color.white)
                )
                ViewCompat.setBackgroundTintList(
                    mapSatellite,
                    ContextCompat.getColorStateList(this@PlayBackRoute, R.color.white)
                )
                ViewCompat.setBackgroundTintList(
                    mapTerrain,
                    ContextCompat.getColorStateList(this@PlayBackRoute, R.color.white)
                )
            }

            2 -> {
                ViewCompat.setBackgroundTintList(
                    mapNormal,
                    ContextCompat.getColorStateList(this@PlayBackRoute, R.color.white)
                )
                ViewCompat.setBackgroundTintList(
                    mapHybrid,
                    ContextCompat.getColorStateList(this@PlayBackRoute, R.color.white)
                )
                ViewCompat.setBackgroundTintList(
                    mapSatellite,
                    ContextCompat.getColorStateList(this@PlayBackRoute, R.color.colorPrimaryDark)
                )
                ViewCompat.setBackgroundTintList(
                    mapTerrain,
                    ContextCompat.getColorStateList(this@PlayBackRoute, R.color.white)
                )
            }

            3 -> {
                ViewCompat.setBackgroundTintList(
                    mapNormal,
                    ContextCompat.getColorStateList(this@PlayBackRoute, R.color.white)
                )
                ViewCompat.setBackgroundTintList(
                    mapHybrid,
                    ContextCompat.getColorStateList(this@PlayBackRoute, R.color.white)
                )
                ViewCompat.setBackgroundTintList(
                    mapSatellite,
                    ContextCompat.getColorStateList(this@PlayBackRoute, R.color.white)
                )
                ViewCompat.setBackgroundTintList(
                    mapTerrain,
                    ContextCompat.getColorStateList(this@PlayBackRoute, R.color.colorPrimaryDark)
                )
            }

            4 -> {
                ViewCompat.setBackgroundTintList(
                    mapNormal,
                    ContextCompat.getColorStateList(this@PlayBackRoute, R.color.white)
                )
                ViewCompat.setBackgroundTintList(
                    mapHybrid,
                    ContextCompat.getColorStateList(this@PlayBackRoute, R.color.colorPrimaryDark)
                )
                ViewCompat.setBackgroundTintList(
                    mapSatellite,
                    ContextCompat.getColorStateList(this@PlayBackRoute, R.color.white)
                )
                ViewCompat.setBackgroundTintList(
                    mapTerrain,
                    ContextCompat.getColorStateList(this@PlayBackRoute, R.color.white)
                )
            }

        }
    }

}
