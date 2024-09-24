package com.shazcom.gps.app.ui.activities

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.shazcom.gps.app.GPSWoxApp
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.Items
import com.shazcom.gps.app.ui.BaseActivity
import com.shazcom.gps.app.utils.getCar
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_show_point.*


class ShowPoint : BaseActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    val listItem = arrayListOf<Items>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_point)

        toolBar.title = "Show Point"
        toolBar.setNavigationOnClickListener { finish() }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        val app = application as GPSWoxApp
        val deviceData = app?.getDeviceList()

        deviceData?.let {

            for (data in it) {
                if(data.items?.isNotEmpty() == true) {
                    listItem.addAll(data.items!!)
                }
            }

            setDevices(listItem)
        }
    }

    private fun setDevices(list: List<Items>) {
        val deviceAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            list
        )

        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        deviceSpinner.adapter = deviceAdapter

        deviceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                moveToMap(listItem[position])
            }

        }

        spinnerLayout.visibility = View.VISIBLE
    }

    private fun moveToMap(items: Items) {

        mMap?.clear()

        mMap?.addMarker(
            MarkerOptions().position(LatLng(items.lat!!, items.lng!!))
                .flat(true)
                .icon(
                    BitmapDescriptorFactory.fromResource(getCar(items.icon_color!!))
                )
        )

        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(items.lat!!, items.lng!!), 15f))
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        googleMap?.uiSettings?.setAllGesturesEnabled(true)
        mMap = googleMap
        mMap?.setOnMapClickListener { latLng ->
            drawNewMaker(latLng)
        }
    }

    private fun drawNewMaker(latLng: LatLng?) {
        mMap?.clear()
        mMap?.addMarker(
            MarkerOptions().position(latLng)
                .flat(true)
                .icon(
                    BitmapDescriptorFactory.fromResource(R.drawable.map_pin_start)
                )
        )

        latLng?.let {
            locLatitude.text = it?.latitude!!.toString()
            locLongitude.text = it?.longitude!!.toString()
            val location = Location("new_loc")
            location.latitude = it.latitude
            location.longitude = it.longitude
            locationAddress.text = "Fetching Address ..."
            startIntentService(location)
        }
    }

    override fun popUpAddress(addressOutput: String) {
        locationAddress.text = addressOutput
    }
}