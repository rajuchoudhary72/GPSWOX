package com.shazcom.gps.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.ToolsRepository
import com.shazcom.gps.app.data.response.ReportData
import com.shazcom.gps.app.data.response.ReportList
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.BaseFragment
import com.shazcom.gps.app.ui.adapter.ReportListAdapter
import com.shazcom.gps.app.ui.viewmodal.ToolsViewModel
import kotlinx.android.synthetic.main.fragment_report_list.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.support.closestKodein
import org.kodein.di.generic.instance

class ReportList : BaseFragment(), KodeinAware {

    override val kodein by closestKodein()
    private val localDB: LocalDB by instance<LocalDB>()
    private val repository: ToolsRepository by instance<ToolsRepository>()
    private var toolsViewModel: ToolsViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_report_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolsViewModel = ViewModelProvider(this).get(ToolsViewModel::class.java)
        toolsViewModel?.toolsRepository = repository
        loadReports()

        reportList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && addReport.isShown) {
                    addReport.visibility = View.INVISIBLE
                } else if (dy <= 0 && !addReport.isShown) {
                    addReport.show()
                }
            }
        })

        addReport.setOnClickListener {
            findNavController().navigate(R.id.action_reports_to_report)
        }
    }

    private fun loadReports() {
        toolsViewModel?.getReports("en", localDB.getToken()!!, 1)
            ?.observe(requireActivity(), Observer { resources ->
                if(!isVisible) { return@Observer }
                when (resources.status) {
                    Status.SUCCESS -> {
                        progressBar.visibility = View.INVISIBLE
                        processData(resources.data?.items)
                    }
                    Status.ERROR -> {
                        progressBar.visibility = View.INVISIBLE
                    }
                    Status.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                    }
                }
            })
    }

    private fun processData(items: ReportList?) {
        items?.let {
            reportList.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter =
                    ReportListAdapter(
                        items?.reports?.data,
                        { reportData -> editItem(reportData) },
                        { reportData ->
                            deleteItem(reportData)
                        })
            }
        }
    }


    private fun editItem(reportData: ReportData) {
        val bundle = Bundle()
        bundle.putParcelable("report", reportData)
        findNavController().navigate(R.id.action_reports_to_report, bundle)
    }

    private fun deleteItem(reportData: ReportData) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Do you want to delete this Item")
        builder.setPositiveButton(
            "Proceed"
        ) { dialog, which ->
            dialog.dismiss()
            deleteItemApi(reportData.id)
        }

        builder.setNegativeButton(
            "Cancel"
        ) { dialog, which -> dialog?.dismiss() }

        builder.create().show()
    }

    private fun deleteItemApi(id: Int) {
        toolsViewModel?.destroyReport("en", localDB.getToken()!!, id)
            ?.observe(requireActivity(), Observer { resources ->
                when (resources.status) {
                    Status.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                    }
                    Status.ERROR -> {
                        progressBar.visibility = View.INVISIBLE
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Status.SUCCESS -> {
                        progressBar.visibility = View.INVISIBLE
                        if (resources?.data?.status == 1) {
                            loadReports()
                            Toast.makeText(
                                requireContext(),
                                "Report Delete Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
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
}