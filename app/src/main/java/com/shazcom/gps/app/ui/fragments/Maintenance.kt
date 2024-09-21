package com.shazcom.gps.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.shazcom.gps.app.GPSWoxApp
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.Services
import com.shazcom.gps.app.ui.BaseFragment
import com.shazcom.gps.app.ui.adapter.MaintenanceAdapter
import kotlinx.android.synthetic.main.empty_layout.*
import kotlinx.android.synthetic.main.fragment_maintenance.*

class Maintenance : BaseFragment() {

    private val serviceList = ArrayList<Services>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maintenance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var app = requireActivity().application as GPSWoxApp
        app?.getDeviceList()?.forEach { deviceData ->
            deviceData.items.forEach { items ->
                items?.device_data?.services?.forEach { service ->
                    service.deviceName = items.name
                    serviceList.add(service)
                }
             }
        }.also {
            loadMaintenance()
        } ?: kotlin.run {
            emptyText.text = getString(R.string.no_data_found)
            emptyText.visibility = View.VISIBLE
        }
    }

    private fun loadMaintenance() {
        maintenanceList.apply {
            layoutManager = LinearLayoutManager(this@Maintenance.context)
            adapter = MaintenanceAdapter(serviceList)
        }
    }
}