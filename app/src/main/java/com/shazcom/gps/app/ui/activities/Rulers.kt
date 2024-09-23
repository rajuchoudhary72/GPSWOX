package com.shazcom.gps.app.ui.activities

import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.android.libraries.maps.CameraUpdateFactory
import com.shazcom.gps.app.R
import com.shazcom.gps.app.ui.BaseActivity
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import com.shazcom.gps.app.GPSWoxApp
import com.shazcom.gps.app.data.response.Items
import com.shazcom.gps.app.databinding.ActivityRulersBinding
import com.shazcom.gps.app.utils.getCar

import java.text.DecimalFormat

class Rulers : BaseActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityRulersBinding
    private var mMap: GoogleMap? = null
    private val locationList = ArrayList<Location>()
    private val decimalFormat = DecimalFormat("#.##")
    val listItem = arrayListOf<Items>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityRulersBinding.inflate(layoutInflater)
        setContentView(binding.root)

       binding. toolBar.title = "Rulers"
        binding. toolBar.setNavigationOnClickListener { finish() }
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        binding. clearBtn.setOnClickListener {
            locationList.clear()
            mMap?.clear()
            binding.distance.text = "Select at least 2 points"
        }

        val app = application as GPSWoxApp
        val deviceData = app?.getDeviceList()
        deviceData?.let {

            for (data in it) {
                listItem.addAll(data.items)
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
        binding. deviceSpinner.adapter = deviceAdapter

        binding. deviceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

        binding. spinnerLayout.visibility = View.VISIBLE
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

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        googleMap?.uiSettings?.setAllGesturesEnabled(true)
        mMap = googleMap
        mMap?.setOnMapClickListener { latLng ->
            drawNewMaker(latLng)
        }
    }

    private fun drawNewMaker(latLng: LatLng?) {

        latLng?.let {
            locationList.add(getLocationObject(it)!!)

            mMap?.addMarker(
                MarkerOptions().position(latLng)
                    .flat(true)
                    .icon(
                        BitmapDescriptorFactory.fromResource(R.drawable.map_pin_start)
                    )
            )
        }

        loadDistance()
    }

    private fun getLocationObject(latLng: LatLng?): Location? {
        val location = Location("")
        location.latitude = latLng?.latitude!!
        location.longitude - latLng.longitude
        return location
    }

    private fun loadDistance() {
        if (locationList.size >= 2) {
            var measureDistance = 0.0
            var i = 0
            var j = 1

            while (j <= (locationList.size - 1)) {
                val locA = locationList[i]
                val locB = locationList[j]
                measureDistance += locA.distanceTo(locB)
                i++
                j++

                binding. distance.text = "Distance : ${decimalFormat.format(measureDistance)} meters"
            }

        } else {
            binding. distance.text = "Select at least 2 points"
        }
    }
}