package com.shazcom.gps.app.ui.dialogs

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
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
import com.google.android.libraries.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.gson.Gson
import com.shazcom.gps.app.data.response.MapIcons
import com.shazcom.gps.app.data.vo.LatLngModel
import kotlinx.android.synthetic.main.dialog_map.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL


class MapDialog(private val poiDialog: POIDialog) : DialogFragment(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private var mMapMarker = ""
    val listItem = arrayListOf<Items>()
    private var mapIcons: MapIcons? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_map, container, false)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(activity!!, R.style.customDialogTheme)
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
                    ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fm: FragmentManager = childFragmentManager
        var supportMapFragment = fm.findFragmentById(R.id.map) as SupportMapFragment?

        if (supportMapFragment == null) {
            supportMapFragment = SupportMapFragment.newInstance()
            supportMapFragment?.let {
                fm.beginTransaction().replace(R.id.mapFrame, supportMapFragment).commit()
                supportMapFragment?.getMapAsync(this)
            }
        }

        closeBtn.setOnClickListener {
            dismiss()
        }

        saveBtn.setOnClickListener {
            mMapMarker.isNotEmpty().let {
                poiDialog.setPoiMapMarker(mMapMarker)
                dismiss()
            }
        }




        if(mapIcons == null) {

            val app = (requireActivity().application) as GPSWoxApp
            val deviceData = app?.getDeviceList()
            deviceData?.let {

                for (data in it) {
                    if(data.items?.isNotEmpty() == true) {
                        listItem.addAll(data.items!!)
                    }
                }

                setDevices(listItem)
            }
        }else {
            deviceSpinner.visibility = View.GONE
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

        mapIcons?.let {
            drawPoiMarker(it)
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

            mMapMarker = "{\\\"lat\\\": ${it?.latitude!!},\\\"lng\\\": ${it?.longitude!!}}"
        }
    }

    fun setPoi(mapIcons: MapIcons) {
        this.mapIcons = mapIcons
    }

    private fun drawPoiMarker(poiMarker: MapIcons) {

        val jsonString = JSONObject(poiMarker.coordinates.replace("\\", "")).toString()
        val latLngBounds: LatLngModel = Gson().fromJson(jsonString, LatLngModel::class.java)
        val poiLocation = LatLng(latLngBounds.lat!!, latLngBounds.lng!!)
        var bitmap: Bitmap? = null

        try {
            CoroutineScope(Dispatchers.IO).launch {
                delay(500)
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

                        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(poiLocation , 15f))
                        poiLocation.let {
                            locLatitude.text = it.latitude.toString()
                            locLongitude.text = it.longitude.toString()
                            mMapMarker = "{\\\"lat\\\": ${it.latitude},\\\"lng\\\": ${it.longitude}}"
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}