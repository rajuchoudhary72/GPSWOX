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
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.MapIcons
import com.shazcom.gps.app.data.vo.LatLngModel
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.LatLngBounds
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.gson.Gson
import com.shazcom.gps.app.databinding.DialogShowGeofenceBinding

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class PoiMapDialog(private val mapIcons: MapIcons) :
    DialogFragment(),
    OnMapReadyCallback {
    private lateinit var binding: DialogShowGeofenceBinding
    private var mMap: GoogleMap? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
     binding=    DialogShowGeofenceBinding.inflate(inflater, container, false)
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

        binding.closeBtn.setOnClickListener {
            dismiss()
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {

        googleMap?.uiSettings?.isZoomControlsEnabled = true
        googleMap?.uiSettings?.setAllGesturesEnabled(true)
        mMap = googleMap

        val jsonString = JSONObject(mapIcons.coordinates.replace("\\","")).toString()
        val coordinates = Gson().fromJson(jsonString, LatLngModel::class.java)
        drawNewMaker(coordinates)
    }

    private fun drawNewMaker(coordinates: LatLngModel?) {
        coordinates?.let {
            val poiLocation = LatLng(coordinates.lat!!, coordinates.lng!!)
            var bitmap: Bitmap? = null

            try {
                CoroutineScope(Dispatchers.IO).launch {
                    val url = URL(mapIcons.map_icon.url)
                    bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                    bitmap?.let {
                        val smallMarker = Bitmap.createScaledBitmap(it, 150, 150, false)
                        withContext(Dispatchers.Main) {
                            bitmap?.let {
                                mMap?.addMarker(
                                    MarkerOptions().position(poiLocation)
                                        .flat(true)
                                        .title(mapIcons.name)
                                        .snippet(mapIcons.description)
                                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                                )?.showInfoWindow()
                            }

                            val widthDp = resources.displayMetrics.run { widthPixels / density }

                            val boundsBuilder = LatLngBounds.Builder()
                            boundsBuilder.include(poiLocation)
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
            } catch (e: Exception) {
                println(e)
            }
        }
    }
}