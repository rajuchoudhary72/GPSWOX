package com.shazcom.gps.app.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.ToolsRepository
import com.shazcom.gps.app.data.response.GeoFenceData
import com.shazcom.gps.app.data.response.GeoFenceResponse
import com.shazcom.gps.app.databinding.ActivityGeofenceBinding
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.BaseActivity
import com.shazcom.gps.app.ui.adapter.GeofenceAdapter
import com.shazcom.gps.app.ui.dialogs.GeoFenceDialog
import com.shazcom.gps.app.ui.dialogs.ShowGeoFenceMap
import com.shazcom.gps.app.ui.viewmodal.ToolsViewModel

import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class GeoFencing : BaseActivity(), KodeinAware {

    private lateinit var binding: ActivityGeofenceBinding
    override val kodein by kodein()
    private val localDB: LocalDB by instance<LocalDB>()
    private val repository: ToolsRepository by instance<ToolsRepository>()
    private var toolsViewModel: ToolsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding= ActivityGeofenceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolBar.setNavigationOnClickListener { finish() }

        toolsViewModel = ViewModelProvider(this).get(ToolsViewModel::class.java)
        toolsViewModel?.toolsRepository = repository
        loadGeofence()

        binding. addGeo.setOnClickListener {
            val geoFenceDialog = GeoFenceDialog(this)
            geoFenceDialog.show(supportFragmentManager, GeoFenceDialog::class.java.name)
        }

        binding. geoList.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 &&   binding.addGeo.isShown) {
                    binding.addGeo.visibility = View.INVISIBLE
                }else if(dy <= 0 && !  binding.addGeo.isShown) {
                    binding.addGeo.show()
                }
            }

        })
    }

    fun loadGeofence() {
        toolsViewModel?.loadGeoFence("en", localDB.getToken()!!)
            ?.observe(this, Observer { resources ->
                when (resources.status) {
                    Status.SUCCESS -> {
                        binding.progressBar.visibility = View.INVISIBLE
                        binding.inc.emptyText.visibility = View.INVISIBLE
                        processData(resources.data!!)
                    }

                    Status.LOADING -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.inc.emptyText.visibility = View.INVISIBLE
                    }

                    Status.ERROR -> {
                        binding.progressBar.visibility = View.INVISIBLE
                        binding.inc. emptyText.visibility = View.VISIBLE
                        binding.inc.emptyText.text = "No geofence data Found"
                    }
                }
            })
    }

    private fun processData(data: GeoFenceResponse) {
        data.let {

            if (data.items.geofences.isNotEmpty()) {
                binding. geoList.apply {
                    layoutManager = LinearLayoutManager(this@GeoFencing)
                    adapter =
                        GeofenceAdapter(
                            data.items.geofences,
                            { geoFenceData -> onItemClick(geoFenceData) },
                            { geoFenceData -> deleteItemClick(geoFenceData) },
                            { geoFenceData -> editItemClick(geoFenceData) })
                }
            } else {
                binding.progressBar.visibility = View.INVISIBLE
                binding.inc.emptyText.visibility = View.VISIBLE
                binding.inc. emptyText.text = getString(R.string.no_geo_fence_data)
            }
        } ?: run {
            binding.progressBar.visibility = View.INVISIBLE
            binding.inc. emptyText.visibility = View.VISIBLE
            binding.inc.emptyText.text = getString(R.string.no_geo_fence_data)
        }
    }

    private fun onItemClick(geoFenceData: GeoFenceData) {
        val showGeoFenceDialog = ShowGeoFenceMap(geoFenceData)
        showGeoFenceDialog.show(supportFragmentManager, ShowGeoFenceMap::class.java.name)
    }

    private fun deleteItemClick(geoFenceData: GeoFenceData) {
        val builder = AlertDialog.Builder(this@GeoFencing)
        builder.setMessage(getString(R.string.delete_alert))
        builder.setPositiveButton(
            getString(R.string.proceed)
        ) { dialog, which ->
            deleteGeoItem(geoFenceData.id)
            dialog.dismiss()
        }

        builder.setNegativeButton(
            "Cancel"
        ) { dialog, which -> dialog?.dismiss() }

        builder.create().show()
    }

    private fun editItemClick(geoFenceData: GeoFenceData) {
        val geoFenceDialog = GeoFenceDialog(this)
        geoFenceDialog.setGeoFenceData(geoFenceData)
        geoFenceDialog.show(supportFragmentManager, GeoFenceDialog::class.java.name)
    }

    private fun deleteGeoItem(id: Int) {
        toolsViewModel?.destroyGeoFence("en", localDB?.getToken()!!, id)
            ?.observe(this, Observer { resources ->
                when (resources.status) {
                    Status.LOADING -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    Status.ERROR -> {
                        binding. progressBar.visibility = View.INVISIBLE
                        Toast.makeText(
                            this@GeoFencing,
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Status.SUCCESS -> {
                        binding.progressBar.visibility = View.INVISIBLE
                        if (resources.data?.status == 1) {
                            Toast.makeText(
                                this@GeoFencing,
                                "Geofence Delete Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            loadGeofence()
                        } else {
                            Toast.makeText(
                                this@GeoFencing,
                                getString(R.string.something_went_wrong),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            })
    }
}