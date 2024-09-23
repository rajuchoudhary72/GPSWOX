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
import com.shazcom.gps.app.data.response.MapIcons
import com.shazcom.gps.app.data.response.UserPoiResponse
import com.shazcom.gps.app.databinding.ActivityPoiBinding
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.BaseActivity
import com.shazcom.gps.app.ui.adapter.UserPoiAdapter
import com.shazcom.gps.app.ui.dialogs.POIDialog
import com.shazcom.gps.app.ui.dialogs.PoiMapDialog
import com.shazcom.gps.app.ui.viewmodal.ToolsViewModel

import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class POI : BaseActivity(), KodeinAware {

    private lateinit var binding: ActivityPoiBinding
    override val kodein by kodein()
    private val localDB: LocalDB by instance<LocalDB>()
    private val repository: ToolsRepository by instance<ToolsRepository>()
    private var toolsViewModel: ToolsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityPoiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolBar.setNavigationOnClickListener { finish() }

        toolsViewModel = ViewModelProvider(this).get(ToolsViewModel::class.java)
        toolsViewModel?.toolsRepository = repository
        loadPoiMarker()

        binding. addPoi.setOnClickListener {
            val poiDialog = POIDialog(this)
            poiDialog.isCancelable = false
            poiDialog.show(supportFragmentManager, POIDialog::class.java.name)
        }

        binding. poiList.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && binding.addPoi.isShown) {
                    binding.addPoi.visibility = View.INVISIBLE
                }else if(dy <= 0 && !binding.addPoi.isShown) {
                    binding. addPoi.show()
                }
            }

        })
    }

    fun loadPoiMarker() {
        toolsViewModel?.loadPoiMarkers("en", localDB.getToken()!!)
            ?.observe(this@POI, Observer { resources ->
                when (resources.status) {
                    Status.SUCCESS -> {
                        binding. progressBar.visibility = View.INVISIBLE
                        binding.inc.  emptyText.visibility = View.INVISIBLE
                        processData(resources.data!!)
                    }

                    Status.LOADING -> {
                        binding. progressBar.visibility = View.VISIBLE
                        binding.inc.emptyText.visibility = View.INVISIBLE
                    }

                    Status.ERROR -> {
                        binding. progressBar.visibility = View.INVISIBLE
                        binding.inc.emptyText.visibility = View.VISIBLE
                        binding.inc. emptyText.text = "No POI Found"
                    }
                }
            })
    }

    private fun processData(data: UserPoiResponse) {
        binding.   poiList.apply {
            layoutManager = LinearLayoutManager(this@POI)
            adapter = UserPoiAdapter(
                data.items.mapIcons,
                { mapIcon -> onItemClick(mapIcon) },
                { mapIcon -> deleteItem(mapIcon) },
                { mapIcon -> editItem(mapIcon) })
        }
    }

    private fun onItemClick(mapIcon: MapIcons) {
        val poiDialog = PoiMapDialog(mapIcon)
        poiDialog.show(supportFragmentManager, PoiMapDialog::class.java.name)
    }

    private fun editItem(mapIcon: MapIcons) {
        val poiDialog = POIDialog(this)
        poiDialog.isCancelable = false
        poiDialog.setMapIcon(mapIcon)
        poiDialog.show(supportFragmentManager, POIDialog::class.java.name)
    }

    private fun deleteItem(mapIcon: MapIcons) {
        val builder = AlertDialog.Builder(this@POI)
        builder.setMessage(getString(R.string.delete_alert))
        builder.setPositiveButton(
            getString(R.string.proceed)
        ) { dialog, which ->
            deletePoiItem(mapIcon.id)
            dialog.dismiss()
        }

        builder.setNegativeButton(
            "Cancel"
        ) { dialog, which -> dialog?.dismiss() }

        builder.create().show()
    }

    private fun deletePoiItem(id: Int) {
        toolsViewModel?.destroyPoiMarker("en", localDB?.getToken()!!, id)
            ?.observe(this, Observer { resources ->
                when (resources.status) {
                    Status.LOADING -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    Status.ERROR -> {
                        binding.progressBar.visibility = View.INVISIBLE
                        Toast.makeText(
                            this@POI,
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Status.SUCCESS -> {
                        binding.  progressBar.visibility = View.INVISIBLE
                        if (resources.data?.status == 1) {
                            Toast.makeText(
                                this@POI,
                                "Map Icon Delete Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            loadPoiMarker()
                        } else {
                            Toast.makeText(
                                this@POI,
                                getString(R.string.something_went_wrong),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            })
    }
}