package com.shazcom.gps.app.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.ToolsRepository
import com.shazcom.gps.app.data.response.AlertData
import com.shazcom.gps.app.data.response.AlertResponse
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.BaseFragment
import com.shazcom.gps.app.ui.activities.Alerts
import com.shazcom.gps.app.ui.activities.Dashboard
import com.shazcom.gps.app.ui.adapter.AlertListAdapter
import com.shazcom.gps.app.ui.viewmodal.ToolsViewModel
import kotlinx.android.synthetic.main.fragment_alert_page.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.support.closestKodein
import org.kodein.di.generic.instance

class AlertPage : BaseFragment(), KodeinAware {


    override val kodein by closestKodein()
    private val localDB: LocalDB by instance<LocalDB>()
    private val repository: ToolsRepository by instance<ToolsRepository>()
    private var toolsViewModel: ToolsViewModel? = null
    private var typeList = listOf(
        "overspeed",
        "idle_duration",
        "ignition_duration",
        "geofence_inout",
        "custom"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alert_page, container, false)
    }


    override fun onDestroyView() {
        super.onDestroyView()

        (requireActivity() as Dashboard).findViewById<ImageView>(R.id.nav_icon).visibility =
            View.VISIBLE
        (requireActivity() as Dashboard).findViewById<ImageView>(R.id.back_icon).visibility =
            View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as Dashboard).findViewById<ImageView>(R.id.nav_icon).visibility =
            View.GONE
        (requireActivity() as Dashboard).findViewById<ImageView>(R.id.back_icon).visibility =
            View.VISIBLE

        toolsViewModel = ViewModelProvider(this).get(ToolsViewModel::class.java)
        toolsViewModel?.toolsRepository = repository

        loadAlerts()

        addAlert.setOnClickListener {
            Intent(requireActivity(), Alerts::class.java).apply {
                startActivityForResult(this, 1190)
            }
        }


        alertList.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && addAlert.isShown) {
                    addAlert.visibility = View.INVISIBLE
                } else if (dy <= 0 && !addAlert.isShown) {
                    addAlert.show()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {

                /* if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                     addAlert.show()
                 }*/

                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    private fun loadAlerts() {
        toolsViewModel?.loadAlerts("en", localDB?.getToken()!!)
            ?.observe(requireActivity(), Observer { resources ->
                if (!isVisible) {
                    return@Observer
                }
                when (resources.status) {


                    Status.SUCCESS -> {
                        progressBar.visibility = View.INVISIBLE
                        processData(resources.data)
                    }
                    Status.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                    }
                    Status.ERROR -> {
                        progressBar.visibility = View.INVISIBLE
                    }
                }
            })
    }

    private fun processData(data: AlertResponse?) {
        data?.let {
            alertList.apply {
                layoutManager = LinearLayoutManager(this@AlertPage.context)
                adapter =
                    AlertListAdapter(
                        data?.items?.alerts?: emptyList(),
                        { alertData -> editItem(alertData) },
                        { alertData -> deleteItem(alertData) })
            }
        }
    }

    private fun editItem(alertData: AlertData) {
        Intent(requireActivity(), Alerts::class.java).apply {
            putExtra("alert", alertData)
            startActivityForResult(this, 1190)
        }
    }

    private fun deleteItem(alertData: AlertData) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Do you want to delete this Item")
        builder.setPositiveButton(
            "Proceed"
        ) { dialog, which ->
            deleteAlertItem(alertData?.id!!)
            dialog.dismiss()
        }

        builder.setNegativeButton(
            "Cancel"
        ) { dialog, which -> dialog?.dismiss() }

        builder.create().show()
    }

    private fun deleteAlertItem(id: Int) {
        toolsViewModel?.destroyAlert("en", localDB?.getToken()!!, id)
            ?.observe(requireActivity(), Observer { resources ->
                when (resources.status) {
                    Status.ERROR -> {
                        progressBar.visibility = View.INVISIBLE
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Status.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        progressBar.visibility = View.INVISIBLE
                        if (resources.data?.status!! == 1) {
                            Toast.makeText(
                                requireContext(),
                                "Alert Delete Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            loadAlerts()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.something_went_wrong),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === 1190) {
            if (resultCode === Activity.RESULT_OK) {
                loadAlerts()
            }
        }
    }
}