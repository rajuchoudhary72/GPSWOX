package com.shazcom.gps.app.ui.dialogs

import android.app.Dialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.GeoFenceData
import com.shazcom.gps.app.data.vo.LatLngModel
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.*
import com.google.gson.Gson
import com.shazcom.gps.app.databinding.DialogShowGeofenceBinding

import org.json.JSONArray
import kotlin.collections.ArrayList


class ShowGeoFenceMap(private val geoFenceData: GeoFenceData) :
    DialogFragment(),
    OnMapReadyCallback {

    private lateinit var binding: DialogShowGeofenceBinding
    private var mMap: GoogleMap? = null
    private var mMapMarker = ArrayList<String>()

    private val polygonList = ArrayList<LatLng>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding=  DialogShowGeofenceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireActivity(), R.style.customDialogTheme)
        dialog.setContentView(R.layout.dialog_show_geofence)
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

        binding. closeBtn.setOnClickListener {
            dismiss()
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        googleMap?.uiSettings?.setAllGesturesEnabled(true)
        mMap = googleMap

        val jsonString = JSONArray(geoFenceData.coordinates).toString()
        val listCoordinates =
            Gson().fromJson(jsonString, Array<LatLngModel>::class.java).toList()

        listCoordinates?.forEach {
            val latLng = LatLng(it.lat!!, it.lng!!)
            drawNewMaker(latLng)
        }

        Handler().postDelayed({
            drawPolygon()
        }, 1000)

    }

    private fun drawNewMaker(latLng: LatLng?) {

        mMap?.addMarker(
            MarkerOptions().position(latLng)
                .flat(true)
                .icon(
                    BitmapDescriptorFactory.fromResource(R.drawable.maps_blue_dot)
                )
        )

        latLng?.let {
            polygonList.add(it)
        }
    }

    private fun drawPolygon() {

        if (polygonList.size > 2) {
            mMap?.addPolygon(
                PolygonOptions()
                    .fillColor(Color.parseColor("${geoFenceData.polygon_color}"))
                    .clickable(false)
                    .addAll(
                        polygonList
                    )
            )

            val widthDp = resources.displayMetrics.run { widthPixels / density }
            val boundsBuilder = LatLngBounds.Builder()
            for (latLngPoint in polygonList) boundsBuilder.include(latLngPoint)
            val routePadding = widthDp / 3
            val latLngBounds = boundsBuilder.build()
            mMap?.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    latLngBounds,
                    routePadding.toInt()
                )
            )

        }
    }
}