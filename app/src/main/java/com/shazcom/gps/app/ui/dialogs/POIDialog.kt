package com.shazcom.gps.app.ui.dialogs

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
import com.shazcom.gps.app.data.response.IconItems
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.activities.POI
import com.shazcom.gps.app.ui.viewmodal.ToolsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.shazcom.gps.app.data.response.MapIcons
import com.shazcom.gps.app.databinding.DialogPoiBinding


import org.kodein.di.KodeinAware
import org.kodein.di.android.support.closestKodein
import org.kodein.di.generic.instance

class POIDialog(private val poiActivity: POI) : BottomSheetDialogFragment(), KodeinAware {

    private lateinit var binding: DialogPoiBinding
    override val kodein by closestKodein()
    private val localDB: LocalDB by instance<LocalDB>()
    private val repository: ToolsRepository by instance<ToolsRepository>()
    private var toolsViewModel: ToolsViewModel? = null
    private var mapIconId = 0
    private var locationStr = ""
    private var mapIconMain : MapIcons? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding=  DialogPoiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolsViewModel = ViewModelProvider(this).get(ToolsViewModel::class.java)
        toolsViewModel?.toolsRepository = repository

        binding.closeBtn.setOnClickListener {
            dismiss()
        }

        binding.   iconPoi.setOnClickListener {
            val poiIconDialogFragment = PoiIconDialog(this)
            poiIconDialogFragment.show(childFragmentManager, PoiIconDialog::class.java.name)
        }

        binding.   poiLocation.setOnClickListener {
            val poiMapDialog = MapDialog(this)
            mapIconMain?.let { mapIcon ->
                poiMapDialog.setPoi(mapIcon)
            }
            poiMapDialog.show(childFragmentManager, MapDialog::class.java.name)
        }

        binding. saveBtn.setOnClickListener {
            mapIconMain?.let {
                updatePoiMarker(it.id)
            }?: kotlin.run {
                savePoiMarker()
            }
        }

        mapIconMain?.let {
            setIconItems(it.map_icon_id)
            setPoiMapMarker(it.coordinates)
            binding.   name.setText(it.name)
            binding.   description.setText(it.description)
        }
    }

    private fun savePoiMarker() {
        toolsViewModel?.savePOIMarker(
            "en",
            localDB.getToken()!!,
            binding.  name.text.toString(),
            binding.  description.text.toString(),
            mapIconId,
            locationStr
        )?.observe(requireActivity(), Observer { resources ->
            when (resources.status) {
                Status.SUCCESS -> {
                    binding.       progressBar.visibility = View.GONE
                    binding.    saveBtn.visibility = View.VISIBLE
                    poiActivity.loadPoiMarker()
                    dismiss()
                }

                Status.LOADING -> {
                    binding. progressBar.visibility = View.VISIBLE
                    binding. saveBtn.visibility = View.INVISIBLE
                }

                Status.ERROR -> {
                    binding. progressBar.visibility = View.GONE
                    binding.  saveBtn.visibility = View.VISIBLE
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

    private fun updatePoiMarker(id: Int) {
        toolsViewModel?.updatePOIMarker(
            "en",
            localDB.getToken()!!,
            id,
            binding.  name.text.toString(),
            binding.  description.text.toString(),
            mapIconId,
            locationStr
        )?.observe(requireActivity(), Observer { resources ->
            when (resources.status) {
                Status.SUCCESS -> {
                    binding.      progressBar.visibility = View.GONE
                    binding.     saveBtn.visibility = View.VISIBLE
                    poiActivity.loadPoiMarker()
                    dismiss()
                }

                Status.LOADING -> {
                    binding.   progressBar.visibility = View.VISIBLE
                    binding.    saveBtn.visibility = View.INVISIBLE
                }

                Status.ERROR -> {
                    binding.  progressBar.visibility = View.GONE
                    binding.  saveBtn.visibility = View.VISIBLE
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

    fun setIconItems(iconItems: IconItems) {
        mapIconId = iconItems.id
        binding.  iconPoi.text = "Icon Selected"
    }

    private fun setIconItems(iconItemId : Int) {
        mapIconId = iconItemId
        binding. iconPoi.text = "Icon Selected"
    }

    fun setPoiMapMarker(mapMaker: String) {
        locationStr = mapMaker
        binding. poiLocation.text = "Location Selected"
    }

    fun setMapIcon(mapIcon: MapIcons) {
        this.mapIconMain = mapIcon
    }
}