package com.shazcom.gps.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.shazcom.gps.app.R
import com.shazcom.gps.app.ui.BaseFragment
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_history.*

class   HistoryPage : BaseFragment(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        setUpPanel()

        openPanel.setOnClickListener {

        }
    }

    private fun setUpPanel() {
        childFragmentManager.beginTransaction()
            .replace(R.id.rightPane, TimeChooserPanel.instance()).commit()
    }

    fun loadHistory(
        id: Int,
        keyword: String,
        startTime: String = "",
        endTime: String = ""
    ) {
        childFragmentManager.beginTransaction()
            .replace(R.id.rightPane, HistoryListing.instance(id, keyword))
            .addToBackStack(HistoryPage::class.java.name).commit()
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap = googleMap
    }
}