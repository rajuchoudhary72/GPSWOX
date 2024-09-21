package com.shazcom.gps.app.ui.activities

import android.location.Location
import android.os.Bundle
import android.view.View
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.EventData
import com.shazcom.gps.app.ui.BaseActivity
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_event_detail.*
import kotlinx.android.synthetic.main.event_bottom_sheet_layout.*
import kotlin.math.roundToInt

class EventDetail : BaseActivity(), OnMapReadyCallback {

    var mMap: GoogleMap? = null
    var eventBehavior: BottomSheetBehavior<*>? = null
    var eventItem: EventData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)

        eventItem = intent?.getParcelableExtra("eventItem")
        toolBar.title = "${eventItem?.device_name}"
        toolBar.setNavigationOnClickListener { finish() }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        displayEvent()
    }

    private fun setMapPaddingBottom(lat: Double, lng: Double, off: Float) {
        val maxMapPaddingBottom = 1.0f
        mMap?.setPadding(0, 0, 0, (off * maxMapPaddingBottom).roundToInt())

        val itemLocation = LatLng(lat, lng)
        mMap?.moveCamera(CameraUpdateFactory.newLatLng(itemLocation))
        mMap?.animateCamera(CameraUpdateFactory.zoomTo(15f))
    }

    private fun displayEvent() {

        eventDataSheet.visibility = View.VISIBLE
        eventName.text = eventItem?.message
        eventDriver.text = "-"
        eventLatitude.text = eventItem?.latitude?.let { "$it\u00B0" } ?: run { "-" }
        eventLongitude.text = eventItem?.longitude?.let { "$it\u00B0" } ?: run { "-" }
        eventAltitude.text = eventItem?.altitude?.let { "${it}m" } ?: run { "-" }

        var itemLocation = LatLng(eventItem?.latitude!!, eventItem?.longitude!!)
        val location = Location("${eventItem?.device_id}")
        location.latitude = itemLocation.latitude
        location.longitude = itemLocation.longitude
        startIntentService(location)

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

    override fun popUpAddress(addressOutput: String) {
        super.popUpAddress(addressOutput)
        eventAddress.text = "$addressOutput"
    }
}