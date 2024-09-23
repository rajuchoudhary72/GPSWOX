package com.shazcom.gps.app.ui.activities

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.shazcom.gps.app.GPSWoxApp
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.ItemsInner
import com.shazcom.gps.app.ui.BaseActivity
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.shazcom.gps.app.databinding.ActivityHistoryDetailBinding

import kotlin.math.roundToInt

class HistoryDetailPage : BaseActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityHistoryDetailBinding
    var itemsInner: ItemsInner? = null
    var distanceRouteStart: String? = null
    var moveDurationStart: String? = null
    var stopDurationStart: String? = null
    var topSpeedStart: String? = null
    var fuelConsStart: String? = null

    var startBehavior: BottomSheetBehavior<*>? = null
    var endBehavior: BottomSheetBehavior<*>? = null
    var parkingBehavior: BottomSheetBehavior<*>? = null
    var driveBehavior: BottomSheetBehavior<*>? = null
    var eventBehavior: BottomSheetBehavior<*>? = null


    var mMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        itemsInner = (application as GPSWoxApp).getItemInner()
        val deviceName = intent?.getStringExtra("deviceName")
        distanceRouteStart = intent?.getStringExtra("distance_route")
        moveDurationStart = intent?.getStringExtra("move_duration")
        stopDurationStart = intent?.getStringExtra("stop_duration")
        topSpeedStart = intent?.getStringExtra("top_speed")
        fuelConsStart = intent?.getStringExtra("fuel_cons")


        binding.toolBar.title = deviceName
        binding.toolBar.setNavigationOnClickListener { finish() }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        if (itemsInner?.items != null) {
            if (itemsInner?.status != 1) {
                val itemMain = itemsInner?.items!![0]
                val location = Location(deviceName)
                location.latitude = itemMain.lat!!
                location.longitude = itemMain.lng!!
                startIntentService(location)
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap = googleMap
        updateUI(itemsInner)
    }

    private fun updateUI(itemsInner: ItemsInner?) {
        when (itemsInner?.status!!) {
            1 -> {
                displayDrive()
            }

            2 -> {
                displayPark()
            }

            3 -> {
                displayStart()
            }

            4 -> {
                displayEnd()
            }

            5 -> {
                displayEvent()
            }
        }
    }

    override fun popUpAddress(addressOutput: String) {
        when (itemsInner?.status!!) {
            2 -> {
                binding.incParkingBottomSheet.parkingAddress.text = addressOutput
            }

            3 -> {
                binding.incHistoryBottomSheet.startAddress.text = addressOutput
            }

            4 -> {
                binding.incEndBottomSheet.endAddress.text = addressOutput
            }

            5 -> {
                binding.incEventBottomSheet.eventAddress.text = addressOutput
            }
        }
    }

    override fun popUpAddressWithKeyWord(addressOutput: String, keyword: String) {
        when (keyword) {
            "PARK_START" -> {
                binding.incParkingBottomSheet.parkingStartAddress.text = addressOutput
            }

            "PARK_STOP" -> {
                binding.incParkingBottomSheet.parkingStopAddress.text = addressOutput
            }

            "DRIVE_START" -> {
                binding.incDriveBottomSheet.driveStartAddress.text = addressOutput
            }

            "DRIVE_STOP" -> {
                binding.incDriveBottomSheet.driveStopAddress.text = addressOutput
            }
        }
    }


    private fun drawRoute() {
        val routeList = arrayListOf<LatLng>()
        itemsInner?.items?.forEach { routeItem ->
            routeList.add(LatLng(routeItem.latitude!!, routeItem.longitude!!))
        }

        if (routeList.size > 0) {
            mMap?.let {
                val colorPrimary =
                    ContextCompat.getColor(this@HistoryDetailPage, android.R.color.holo_green_dark)
                mMap?.addPolyline(
                    PolylineOptions().color(colorPrimary).geodesic(true).width(4.0f)
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


                val loc1 = Location("startPos")
                val loc2 = Location("endPos")

                loc1.latitude = routeList[0].latitude
                loc1.longitude = routeList[0].longitude

                loc2.latitude = routeList[routeList.size - 1].latitude
                loc2.longitude = routeList[routeList.size - 1].longitude

                startIntentServiceWithKeyword(loc1, "DRIVE_STOP")
                startIntentServiceWithKeyword(loc2, "DRIVE_START")

            }
        }

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

    private fun setMapPaddingBottom(lat: Double, lng: Double, off: Float) {
        val maxMapPaddingBottom = 1.0f
        mMap?.setPadding(0, 0, 0, (off * maxMapPaddingBottom).roundToInt())

        val itemLocation = LatLng(lat, lng)
        mMap?.moveCamera(CameraUpdateFactory.newLatLng(itemLocation))
        mMap?.animateCamera(CameraUpdateFactory.zoomTo(15f))
    }

    @SuppressLint("SetTextI18n")
    private fun displayStart() {

        binding.incDriveBottomSheet.driveDataSheet.visibility = View.GONE
        binding.incEndBottomSheet.endDataSheet.visibility = View.GONE
        binding.incEventBottomSheet.eventDataSheet.visibility = View.GONE
        binding.incParkingBottomSheet.parkDataSheet.visibility = View.GONE
        binding.incHistoryBottomSheet.mapDataSheet.visibility = View.VISIBLE

        binding.incHistoryBottomSheet.startFuelCons.text = fuelConsStart
        binding.incHistoryBottomSheet.startDriver.text =
            itemsInner?.driver?.name?.let { it } ?: run { "-" }
        binding.incHistoryBottomSheet.startLatitude.text =
            "${itemsInner?.items!![0].latitude}\u00B0"
        binding.incHistoryBottomSheet.startLongitude.text =
            "${itemsInner?.items!![0].longitude}\u00B0"
        binding.incHistoryBottomSheet.startAltitude.text = "${itemsInner?.items!![0].altitude}m"
        binding.incHistoryBottomSheet.startRoute.text = distanceRouteStart
        binding.incHistoryBottomSheet.startMoveDuration.text = moveDurationStart
        binding.incHistoryBottomSheet.startStopDuration.text = stopDurationStart
        binding.incHistoryBottomSheet.startTopSpeed.text = topSpeedStart

        val itemMain = itemsInner?.items!![0]
        var itemLocation = LatLng(itemMain?.latitude!!, itemMain?.longitude!!)

        mMap?.addMarker(
            MarkerOptions().position(itemLocation)
                .icon(
                    BitmapDescriptorFactory.fromResource(R.drawable.start_flag)
                )
        )

        mMap?.moveCamera(CameraUpdateFactory.newLatLng(itemLocation))
        mMap?.animateCamera(CameraUpdateFactory.zoomTo(15f))

        startBehavior = BottomSheetBehavior.from(binding.incHistoryBottomSheet.mapDataSheet)
        startBehavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val h = bottomSheet.height.toFloat()
                val off = h * slideOffset
                when (startBehavior?.state) {
                    BottomSheetBehavior.STATE_SETTLING -> {
                        setMapPaddingBottom(itemLocation.latitude, itemLocation.longitude, off)
                    }
                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }
        })

        binding.incHistoryBottomSheet.startAddress.setOnClickListener {
            if (startBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
                startBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
            } else {
                startBehavior?.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
        }

        if (startBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
            startBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
        }
    }

    private fun displayEnd() {

        binding.incDriveBottomSheet.driveDataSheet.visibility = View.GONE
        binding.incEndBottomSheet.endDataSheet.visibility = View.VISIBLE
        binding.incEventBottomSheet.eventDataSheet.visibility = View.GONE
        binding.incParkingBottomSheet.parkDataSheet.visibility = View.GONE
        binding.incHistoryBottomSheet.mapDataSheet.visibility = View.GONE

        binding.incEndBottomSheet.endFuelCons.text = fuelConsStart
        binding.incEndBottomSheet.endDriver.text =
            itemsInner?.driver?.name?.let { it } ?: run { "-" }
        binding.incEndBottomSheet.endLatitude.text = "${itemsInner?.items!![0].latitude}\u00B0"
        binding.incEndBottomSheet.endLongitude.text = "${itemsInner?.items!![0].longitude}\u00B0"
        binding.incEndBottomSheet.endAltitude.text = "${itemsInner?.items!![0].altitude}m"
        binding.incEndBottomSheet.endRoute.text = distanceRouteStart
        binding.incEndBottomSheet.endMoveDuration.text = moveDurationStart
        binding.incEndBottomSheet.endStopDuration.text = stopDurationStart
        binding.incEndBottomSheet.endTopSpeed.text = topSpeedStart

        val itemMain = itemsInner?.items!![0]
        var itemLocation = LatLng(itemMain?.latitude!!, itemMain?.longitude!!)

        mMap?.addMarker(
            MarkerOptions().position(itemLocation)
                .icon(
                    BitmapDescriptorFactory.fromResource(R.drawable.end_flag)
                )
        )

        mMap?.moveCamera(CameraUpdateFactory.newLatLng(itemLocation))
        mMap?.animateCamera(CameraUpdateFactory.zoomTo(15f))

        endBehavior = BottomSheetBehavior.from(binding.incEndBottomSheet.endDataSheet)
        endBehavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val h = bottomSheet.height.toFloat()
                val off = h * slideOffset
                when (endBehavior?.state) {
                    BottomSheetBehavior.STATE_SETTLING -> {
                        setMapPaddingBottom(itemLocation.latitude, itemLocation.longitude, off)
                    }
                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }
        })

        binding.incHistoryBottomSheet.startAddress.setOnClickListener {
            if (endBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
                endBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
            } else {
                endBehavior?.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
        }

        if (endBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
            endBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
        }

    }

    private fun displayDrive() = with(binding.incDriveBottomSheet) {

        driveDataSheet.visibility = View.VISIBLE
        binding.incEndBottomSheet.endDataSheet.visibility = View.GONE
        binding.incEventBottomSheet.eventDataSheet.visibility = View.GONE
        binding.incParkingBottomSheet.parkDataSheet.visibility = View.GONE
        binding.incHistoryBottomSheet.mapDataSheet.visibility = View.GONE

        driveFuelCons.text = "${itemsInner?.fuel_consumption} ltr"
        driveDriver.text = itemsInner?.driver?.name?.let { it } ?: run { "-" }
        driveRoute.text = "${itemsInner?.distance} km"
        driveDuration.text = "${itemsInner?.time}"
        driveTopSpeed.text = "${itemsInner?.topSpeed} km"
        driveCame.text = "${itemsInner?.show}"
        driveLeft.text = "${itemsInner?.left}"

        val itemMain = itemsInner?.items!![0]
        var itemLocation = LatLng(itemMain?.lat!!, itemMain?.lng!!)

        drawRoute()

        driveBehavior = BottomSheetBehavior.from(driveDataSheet)
        driveBehavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val h = bottomSheet.height.toFloat()
                val off = h * slideOffset
                when (driveBehavior?.state) {
                    BottomSheetBehavior.STATE_SETTLING -> {
                        setMapPaddingBottom(itemLocation.latitude, itemLocation.longitude, off)
                    }
                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }
        })

        driveLayout.setOnClickListener {
            if (driveBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
                driveBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
            } else {
                driveBehavior?.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
        }

        if (driveBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
            driveBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayPark() = with(binding.incParkingBottomSheet) {

        binding.incDriveBottomSheet.driveDataSheet.visibility = View.GONE
        binding.incEndBottomSheet.endDataSheet.visibility = View.GONE
        binding.incEventBottomSheet.eventDataSheet.visibility = View.GONE
        parkDataSheet.visibility = View.VISIBLE
        binding.incHistoryBottomSheet.mapDataSheet.visibility = View.GONE

        parkingFuelCons.text = fuelConsStart
        parkingDriver.text = itemsInner?.driver?.name?.let { it } ?: run { "-" }
        parkingLatitude.text = "${itemsInner?.items!![0].latitude}\u00B0"
        parkingLongitude.text = "${itemsInner?.items!![0].longitude}\u00B0"
        parkingAltitude.text = "${itemsInner?.items!![0].altitude}m"
        parkingSpeed.text = distanceRouteStart
        parkingCame.text = "${itemsInner?.show}"
        parkingLeft.text = "${itemsInner?.left}"
        parkingDuration.text = "${itemsInner?.time}"

        val itemMain = itemsInner?.items!![0]
        var itemLocation = LatLng(itemMain?.latitude!!, itemMain?.longitude!!)

        mMap?.addMarker(
            MarkerOptions().position(itemLocation)
                .icon(
                    BitmapDescriptorFactory.fromResource(R.drawable.map_park_pin)
                )
        )

        mMap?.moveCamera(CameraUpdateFactory.newLatLng(itemLocation))
        mMap?.animateCamera(CameraUpdateFactory.zoomTo(15f))

        parkingBehavior = BottomSheetBehavior.from(parkDataSheet)
        parkingBehavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val h = bottomSheet.height.toFloat()
                val off = h * slideOffset
                when (parkingBehavior?.state) {
                    BottomSheetBehavior.STATE_SETTLING -> {
                        setMapPaddingBottom(itemLocation.latitude, itemLocation.longitude, off)
                    }
                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }
        })

        parkingAddress.setOnClickListener {
            if (parkingBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
                parkingBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
            } else {
                parkingBehavior?.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
        }

        if (parkingBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
            parkingBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
        }


        val locData1 = itemsInner?.items!![0]
        val locData2 = itemsInner?.items!![itemsInner?.items!!.size - 1]

        val loc1 = Location("startPos")
        val loc2 = Location("endPos")

        loc1.latitude = locData1?.lat!!
        loc1.longitude = locData1?.lng!!

        loc2.latitude = locData2?.lat!!
        loc2.longitude = locData2?.lng!!

        startIntentServiceWithKeyword(loc1, "PARK_START")
        startIntentServiceWithKeyword(loc2, "PARK_STOP")

    }

    @SuppressLint("SetTextI18n")
    private fun displayEvent() = with(binding.incEventBottomSheet){

        binding.incDriveBottomSheet.driveDataSheet.visibility = View.GONE
        binding.incEndBottomSheet.endDataSheet.visibility = View.GONE
        eventDataSheet.visibility = View.VISIBLE
       binding.incParkingBottomSheet. parkDataSheet.visibility = View.GONE
       binding.incHistoryBottomSheet. mapDataSheet.visibility = View.GONE

        eventName.text = itemsInner?.message
        eventDriver.text = itemsInner?.driver?.name?.let { it } ?: run { "-" }
        eventLatitude.text = itemsInner?.items!![0].lat?.let { "$it\u00B0" } ?: run { "-" }
        eventLongitude.text = itemsInner?.items!![0].lng?.let { "$it\u00B0" } ?: run { "-" }
        eventAltitude.text = itemsInner?.items!![0].altitude?.let { "${it}m" } ?: run { "-" }

        val itemMain = itemsInner?.items!![0]
        var itemLocation = LatLng(itemMain?.lat!!, itemMain?.lng!!)

        mMap?.addMarker(
            MarkerOptions().position(itemLocation)
                .icon(
                    BitmapDescriptorFactory.fromResource(R.drawable.map_event_pin)
                )
        )

        mMap?.moveCamera(CameraUpdateFactory.newLatLng(itemLocation))
        mMap?.animateCamera(CameraUpdateFactory.zoomTo(15f))

        eventBehavior = BottomSheetBehavior.from(eventDataSheet)
        eventBehavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val h = bottomSheet.height.toFloat()
                val off = h * slideOffset
                when (eventBehavior?.state) {
                    BottomSheetBehavior.STATE_SETTLING -> {
                        setMapPaddingBottom(itemLocation.latitude, itemLocation.longitude, off)
                    }
                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }
        })

        eventAddress.setOnClickListener {
            if (eventBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
                eventBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
            } else {
                eventBehavior?.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
        }

        if (eventBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
            eventBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
        }

    }
}

