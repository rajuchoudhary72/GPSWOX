package com.shazcom.gps.app.ui.dialogs

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.ToolsRepository
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.activities.GeoFencing
import com.shazcom.gps.app.ui.viewmodal.ToolsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.shazcom.gps.app.data.response.GeoFenceData
import com.shazcom.gps.app.databinding.DialogGeofenceBinding

import org.kodein.di.KodeinAware
import org.kodein.di.android.support.closestKodein
import org.kodein.di.generic.instance

class GeoFenceDialog(private val geoFencing: GeoFencing) : BottomSheetDialogFragment(),
    KodeinAware {

    private lateinit var binding: DialogGeofenceBinding
    override val kodein by closestKodein()
    private val localDB: LocalDB by instance<LocalDB>()
    private val repository: ToolsRepository by instance<ToolsRepository>()
    private var toolsViewModel: ToolsViewModel? = null
    private var polygonColor = "ffa500"
    private var locationStr = ""

    private var geoFenceDataMain: GeoFenceData? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogGeofenceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolsViewModel = ViewModelProvider(this).get(ToolsViewModel::class.java)
        toolsViewModel?.toolsRepository = repository
        with(binding) {
            closeBtn.setOnClickListener {
                dismiss()
            }

            addPolygonColor.setOnClickListener {
                val colorDialog = ColorDialog(this@GeoFenceDialog)
                colorDialog.show(childFragmentManager, ColorDialog::class.java.name)
            }

            addGeoFence.setOnClickListener {
                val geoFenceMapDialog = GeoFenceMapDialog(polygonColor, this@GeoFenceDialog)
                geoFenceMapDialog.updatePolyGon(geoFenceDataMain?.coordinates ?: "")
                geoFenceMapDialog.show(childFragmentManager, GeoFenceMapDialog::class.java.name)
            }

            saveBtn.setOnClickListener {
                geoFenceDataMain?.let {
                    updateGeofence(it.id)
                } ?: run {
                    saveGeoFence()
                }
            }

            geoFenceDataMain?.let {
                addPolygonColor(it.polygon_color)
                addGeoFence(it.coordinates)
                name.setText(it.name)
            }
        }
    }

    private fun saveGeoFence()= with(binding) {

        if (polygonColor.length > 6) {
            polygonColor = polygonColor.substring(2, 8)
        }

        toolsViewModel?.saveGeofence(
            "en",
            localDB.getToken()!!,
            name.text.toString(),
            "#${polygonColor}",
            locationStr
        )?.observe(requireActivity(), Observer { resources ->
            when (resources.status) {
                Status.SUCCESS -> {
                    progressBar.visibility = View.GONE
                    saveBtn.visibility = View.VISIBLE
                    geoFencing.loadGeofence()
                    dismiss()
                }

                Status.LOADING -> {
                    progressBar.visibility = View.VISIBLE
                    saveBtn.visibility = View.INVISIBLE
                }

                Status.ERROR -> {
                    saveBtn.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.valid_proper_data),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        })
    }

    private fun updateGeofence(id: Int)= with(binding) {

        if (polygonColor.length > 6 && polygonColor.length != 7) {
            polygonColor = polygonColor.substring(2, 8)
        }

        if (!polygonColor.startsWith("#")) {
            polygonColor = "#${polygonColor}"
        }

        toolsViewModel?.updateGeofence(
            "en",
            localDB.getToken()!!,
            id,
            name.text.toString(),
            "${polygonColor}",
            locationStr
        )?.observe(requireActivity(), Observer { resources ->
            when (resources.status) {
                Status.SUCCESS -> {
                    progressBar.visibility = View.GONE
                    saveBtn.visibility = View.VISIBLE
                    geoFencing.loadGeofence()
                    dismiss()
                }

                Status.LOADING -> {
                    progressBar.visibility = View.VISIBLE
                    saveBtn.visibility = View.INVISIBLE
                }

                Status.ERROR -> {
                    saveBtn.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.valid_proper_data),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        })
    }

    fun addPolygonColor(color: String) = with(binding){
        polygonColor = color
        if (color.startsWith("#")) {
            selectedColor.setBackgroundColor(Color.parseColor("${color}"))
        } else {
            selectedColor.setBackgroundColor(Color.parseColor("#${color}"))
        }
        addPolygonColor.text = "Color Selected"
    }


    fun addGeoFence(geofence: String) {
        locationStr = geofence
        binding.addGeoFence.text = "Geofence Selected"
    }

    fun setGeoFenceData(geoFenceData: GeoFenceData) {
        this.geoFenceDataMain = geoFenceData
    }
}