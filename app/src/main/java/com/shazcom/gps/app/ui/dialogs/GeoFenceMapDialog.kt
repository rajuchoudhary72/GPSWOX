package com.shazcom.gps.app.ui.dialogs

import android.app.Dialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.shazcom.gps.app.GPSWoxApp
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.Items
import com.shazcom.gps.app.utils.getCar
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.*
import kotlinx.android.synthetic.main.dialog_map.*
import kotlinx.android.synthetic.main.dialog_map.closeBtn
import kotlinx.android.synthetic.main.dialog_map.saveBtn
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.json.JSONArray

class GeoFenceMapDialog(private val color: String, private val geoFenceDialog: GeoFenceDialog) :
    DialogFragment(),
    OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private var mMapMarker = ArrayList<String>()
    private var app: GPSWoxApp? = null
    val listItem = arrayListOf<Items>()
    var coordinateObject = ""
    private val polygonList = ArrayList<LatLng>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_map, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), R.style.customDialogTheme)
        dialog.setContentView(R.layout.dialog_map)
        return dialog
    }

    override fun onStart() {
        super.onStart()

        if (dialog != null) {
            dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                dialog?.window?.statusBarColor =
                    ContextCompat.getColor(context!!, R.color.colorPrimaryDark)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomLayout.visibility = View.GONE

        val fm: FragmentManager = childFragmentManager
        var supportMapFragment = fm.findFragmentById(R.id.map) as SupportMapFragment?

        if (supportMapFragment == null) {
            supportMapFragment = SupportMapFragment.newInstance()
            supportMapFragment?.let {
                fm.beginTransaction().replace(R.id.mapFrame, supportMapFragment).commit()
                supportMapFragment.getMapAsync(this)
            }
        }

        closeBtn.setOnClickListener {
            dismiss()
        }

        saveBtn.setOnClickListener {
            mMapMarker.isNotEmpty().let {
                geoFenceDialog.addGeoFence(mMapMarker.toString())
                dismiss()
            }
        }


        if(coordinateObject.isEmpty()) {

            app = requireActivity().application as GPSWoxApp
            val deviceData = app?.getDeviceList()

            deviceData?.let {

                for (data in it) {
                    listItem.addAll(data.items)
                }

                setDevices(listItem)
            }
        }else {
            deviceSpinner.visibility = View.GONE
        }


        clearPolygons.setOnClickListener {
            coordinateObject = ""
            polygonList.clear()
            if(deviceSpinner.isVisible) {
                moveToMap(listItem[deviceSpinner.selectedItemPosition])
            }else{
                mMap?.clear()
            }
        }

    }

    private fun setDevices(list: List<Items>) {
        val deviceAdapter = ArrayAdapter(
            requireContext(),
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
        polygonList.clear()
        mMapMarker.clear()


        mMap?.addMarker(
            MarkerOptions().position(LatLng(items.lat!!, items.lng!!))
                .flat(true)
                .icon(
                    BitmapDescriptorFactory.fromResource(getCar(items.icon_color!!))
                )
        )

        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(items.lat!!, items.lng!!), 15f))

        updatePolygons(coordinateObject)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        googleMap?.uiSettings?.setAllGesturesEnabled(true)
        mMap = googleMap

        mMap?.setOnMapClickListener { latLng ->
            drawNewMaker(latLng)
        }


        updatePolygons(coordinateObject)
    }

    private fun drawNewMaker(latLng: LatLng?) {

        mMap?.addMarker(
            MarkerOptions().position(latLng)
                .flat(true)
                .icon(
                    BitmapDescriptorFactory.fromResource(R.drawable.map_pin_start)
                )
        )

        latLng?.let {
            polygonList.add(it)
            drawPolygon()
        }
    }

    private fun drawPolygon() {

        var polyColor = color

        if (!polyColor.startsWith("#")) {
            polyColor = "#${color}"
        }

        if (polygonList.size > 2) {
            mMap?.addPolygon(
                PolygonOptions()
                    .fillColor(Color.parseColor(polyColor))
                    .clickable(false)
                    .addAll(
                        polygonList
                    )
            )

            makePolygonLocationString()
            clearPolygons.visibility = View.VISIBLE
        } else {
            clearPolygons.visibility = View.GONE
        }
    }

    private fun makePolygonLocationString() {
        mMapMarker.clear()
        polygonList.forEachIndexed { index, latLng ->
            mMapMarker.add("{\"lat\": ${latLng.latitude},\"lng\": ${latLng.longitude}}")
        }
    }

    fun updatePolyGon(coordinateObject: String) {
        this.coordinateObject = coordinateObject
    }

    private fun updatePolygons(coordinatesObject: String) = runBlocking {

        if (coordinatesObject.isEmpty()) {
            return@runBlocking
        }

        try {
            delay(1000)
            Log.e("coordinates", "${JSONArray(coordinatesObject)}")
            val coordinates = JSONArray(coordinatesObject)
            for (c in 0 until coordinates.length()) {
                val latlngObj = coordinates.getJSONObject(c)
                polygonList.add(LatLng(latlngObj.getDouble("lat"), latlngObj.getDouble("lng")))
            }
            Log.d("Polygon List", "$polygonList")
            drawPolygon()
            zoomMap(mMap, polygonList)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


    private fun zoomMap(
        googleMap: GoogleMap?,
        lstLatLngRoute: List<LatLng?>?
    ) {
        if (googleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return
        val boundsBuilder = LatLngBounds.Builder()
        for (latLngPoint in lstLatLngRoute) boundsBuilder.include(latLngPoint)
        val routePadding = 300
        val latLngBounds = boundsBuilder.build()
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding))
    }
}