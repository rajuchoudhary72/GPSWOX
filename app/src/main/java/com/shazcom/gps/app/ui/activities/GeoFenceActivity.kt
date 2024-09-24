package com.shazcom.gps.app.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.maps.model.Circle
import com.google.android.libraries.maps.model.CircleOptions
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.android.libraries.maps.model.PolygonOptions
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.ToolsRepository
import com.shazcom.gps.app.data.response.BaseResponse
import com.shazcom.gps.app.data.response.Items
import com.shazcom.gps.app.databinding.ActivityGeoBinding

import com.shazcom.gps.app.network.internal.Resource
import com.shazcom.gps.app.ui.viewmodal.ToolsViewModel
import com.shazcom.gps.app.utils.getCar

import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance


class GeoFenceActivity : AppCompatActivity(), OnMapReadyCallback, KodeinAware {

    private lateinit var binding: ActivityGeoBinding
    private val mViewModel: ToolsViewModel by viewModels()

    override val kodein by closestKodein()
    private val localDB: LocalDB by instance()
    private val repository: ToolsRepository by instance()

    private lateinit var mapFragment: SupportMapFragment

    private var mGoogleMap: GoogleMap? = null

    private var circleGeoFence: Circle? = null

    private var isCircleGeoFencing: Boolean = true

    private val markerCoordinates = mutableListOf<LatLng>()

    private var deviceItem: Items? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding= ActivityGeoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deviceItem = intent.getParcelableExtra("item")
        mViewModel.toolsRepository = repository

        binding.toolBar.setNavigationOnClickListener { onBackPressed() }

        binding.toolBar.title = deviceItem?.name

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnFencingType.addOnButtonCheckedListener { group, checkedId, isChecked ->
            isCircleGeoFencing = checkedId == R.id.btn_circle
            binding.groupCircleGeoFenceViews.isVisible = isCircleGeoFencing
            binding.textCaption.text =
                if (isCircleGeoFencing) "Click map to adjust fence center, drag control bar to adjust size" else "Click on map to create a polygon selection"
            clearMap()
        }

        binding.seekbarRadius.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                circleGeoFence?.radius = value.toDouble()
                binding.labelSize.text = value.toString() + "m"
            }
        }

        binding.btnClearPolygon.setOnClickListener {
            clearMap()
        }

        binding.btnSave.setOnClickListener {
            if (isDataValid()) {
                mViewModel.saveGeofence(
                    lang = "en",
                    userHash = localDB.getToken()!!,
                    name = binding.name.text.toString(),
                    polygonColor = "#FFA500",
                    polygon = if (isCircleGeoFencing) null else markerCoordinates.asJsonArray(),
                    type = if (isCircleGeoFencing) "circle" else "polygon",
                    center = if (isCircleGeoFencing) circleGeoFence?.center?.let { latLng ->
                        JsonObject().apply {
                            addProperty("lat", latLng.latitude)
                            addProperty("lng", latLng.longitude)
                        }.toString()
                    } else null,
                    radius = if (isCircleGeoFencing) binding.seekbarRadius.value.toString() else null,
                ).observe(this) { response: Resource<BaseResponse> ->
                    binding.progressBar.isVisible = response.isLoading()

                    response.getErrorIfExist()?.let { errorMsg ->
                        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                    }

                    response.getDataOrNull()?.let { _ ->
                        Toast.makeText(this, "Geo Fence added successfully.", Toast.LENGTH_SHORT)
                            .show()
                        onBackPressed()
                    }
                }
            } else {
                Toast.makeText(this, "Complete GeoFence", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isDataValid(): Boolean {
        if (binding.name.text.toString().isEmpty()) {
            return false
        } else if (isCircleGeoFencing && circleGeoFence==null) {
            return false
        } else {
            if (isCircleGeoFencing.not() && (markerCoordinates.size > 2).not()) {
                return false
            }
        }
        return true
    }

    private fun clearMap() {
        mGoogleMap?.clear()
        circleGeoFence = null
        markerCoordinates.clear()
        binding.btnClearPolygon.isVisible = false
        addCarMarker()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        addCarMarker()

        deviceItem?.let {
            val carLocation = LatLng(deviceItem!!.lat!!, deviceItem!!.lng!!)
            addCircle(carLocation)
        }


        mGoogleMap?.setOnMapClickListener { latLng ->
            addMarker(latLng)
        }
    }

    private fun addCarMarker() {
        deviceItem?.let {
            val carLocation = LatLng(deviceItem!!.lat!!, deviceItem!!.lng!!)
            mGoogleMap?.addMarker(
                MarkerOptions().position(carLocation)
                    .flat(true)
                    .title(deviceItem?.name!!)
                    .icon(
                        BitmapDescriptorFactory.fromResource(getCar(deviceItem?.icon_color!!))
                    )
            )
            mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(carLocation, 15F));
        }
    }


    private fun addMarker(latLng: LatLng?) {
        latLng ?: return
        if (isCircleGeoFencing) {
            mGoogleMap?.clear()
            addCarMarker()
        }

        mGoogleMap?.addMarker(
            MarkerOptions().position(latLng)
                .flat(true)
                .icon(
                    BitmapDescriptorFactory.fromResource(R.drawable.map_pin_start)
                )
        )


        mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F));

        if (isCircleGeoFencing) {
            markerCoordinates.clear()
            markerCoordinates.add(latLng)
            addCircle(latLng)
        } else {
            markerCoordinates.add(latLng)
            if (markerCoordinates.size > 2) {
                mGoogleMap?.addPolygon(
                    PolygonOptions()
                        .addAll(markerCoordinates)
                        .fillColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        .strokeColor(ContextCompat.getColor(this, R.color.app_logo_color))

                )
            }
        }

        binding.btnClearPolygon.isVisible = markerCoordinates.size>2
    }

    private fun addCircle(latLng: LatLng?) {
        circleGeoFence = mGoogleMap?.addCircle(
            CircleOptions()
                .fillColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .strokeColor(ContextCompat.getColor(this, R.color.app_logo_color))
                .radius(binding.seekbarRadius.value.toDouble())
                .center(latLng)
        )
    }

    private fun MutableList<LatLng>.asJsonArray(): String {
        val array = JsonArray()
        forEach { latLng ->
            array.add(JsonObject().apply {
                addProperty("lat", latLng.latitude)
                addProperty("lng", latLng.longitude)
            })
        }
        return array.toString()
    }

}


