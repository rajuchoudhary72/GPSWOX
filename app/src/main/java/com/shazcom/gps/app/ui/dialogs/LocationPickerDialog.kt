package com.shazcom.gps.app.ui.dialogs

import android.app.Dialog
import android.location.Location
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
import com.shazcom.gps.app.data.vo.MapData
import com.shazcom.gps.app.ui.activities.TaskPage
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.dialog_loc_picker.*
import kotlinx.android.synthetic.main.dialog_loc_picker.closeBtn
import kotlinx.android.synthetic.main.dialog_loc_picker.saveBtn

class LocationPickerDialog(private val isPickUp: Boolean, private val taskDialog: AddTaskDialog) :
    DialogFragment(),
    OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private var mMapMarker = ""
    private val mapData: MapData = MapData()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_loc_picker, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(activity!!, R.style.customDialogTheme)
        dialog.setContentView(R.layout.dialog_loc_picker)
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
            mMapMarker?.isNotEmpty()?.let {
                taskDialog.saveMapData(mapData)
                dismiss()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        googleMap?.uiSettings?.setAllGesturesEnabled(true)
        mMap = googleMap
        mMap?.setOnMapClickListener { latLng ->
            drawNewMaker(latLng)
        }
    }

    fun showAddress(addressOutPut: String) {
        locationAddress.text = addressOutPut
        mapData.address = addressOutPut
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
            (requireActivity() as TaskPage).startIntentService(location)
            mapData.lat = it.latitude
            mapData.lng = it.longitude
            mapData.isPickUp = isPickUp
        }
    }
}