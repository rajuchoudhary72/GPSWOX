package com.shazcom.gps.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.shazcom.gps.app.GPSWoxApp
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.Services
import com.shazcom.gps.app.databinding.FragmentMaintenanceBinding
import com.shazcom.gps.app.ui.BaseFragment
import com.shazcom.gps.app.ui.adapter.MaintenanceAdapter


class Maintenance : BaseFragment() {

    private lateinit var binding: FragmentMaintenanceBinding
    private val serviceList = ArrayList<Services>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         binding=FragmentMaintenanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var app = requireActivity().application as GPSWoxApp
        app?.getDeviceList()?.forEach { deviceData ->
            deviceData.items?.forEach { items ->
                items?.device_data?.services?.forEach { service ->
                    service.deviceName = items.name
                    serviceList.add(service)
                }
            }
        }.also {
            loadMaintenance()
        } ?: kotlin.run {
            binding.inc. emptyText.text = getString(R.string.no_data_found)
            binding.inc. emptyText.visibility = View.VISIBLE
        }
    }

    private fun loadMaintenance() {
        binding.  maintenanceList.apply {
            layoutManager = LinearLayoutManager(this@Maintenance.context)
            adapter = MaintenanceAdapter(serviceList)
        }
    }
}