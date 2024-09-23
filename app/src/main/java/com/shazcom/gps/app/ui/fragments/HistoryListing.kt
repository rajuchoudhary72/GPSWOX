package com.shazcom.gps.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.CommonViewRepository
import com.shazcom.gps.app.data.response.HistoryData
import com.shazcom.gps.app.databinding.LayoutHistoryListingBinding
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.BaseFragment
import com.shazcom.gps.app.ui.viewmodal.CommonViewModel
import com.shazcom.gps.app.utils.getCurrentDay
import com.shazcom.gps.app.utils.getNewTimeFormat
import com.shazcom.gps.app.utils.getPreviousDays
import com.shazcom.gps.app.utils.nextDay

import org.kodein.di.KodeinAware
import org.kodein.di.android.support.closestKodein
import org.kodein.di.generic.instance

class HistoryListing(
    private val deviceId: Int,
    private val keyword: String,
    private val starDate: String = "",
    private val endDate: String = ""
) : BaseFragment(), KodeinAware {

    private lateinit var binding: LayoutHistoryListingBinding
    private var startDateTime: String? = null
    private var endDateTime: String? = null

    override val kodein by closestKodein()
    private val localDB: LocalDB by instance()
    private val repository: CommonViewRepository by instance()
    private var commonViewModel: CommonViewModel? = null

    companion object {
        fun instance(deviceId: Int, keyword: String, startDate: String = "", endDate: String = "") =
            HistoryListing(deviceId, keyword, startDate, endDate)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding= LayoutHistoryListingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        commonViewModel = ViewModelProvider(this).get(CommonViewModel::class.java)
        commonViewModel?.commonViewRepository = repository

        binding. clearAll.setOnClickListener {
            parentFragment?.childFragmentManager?.popBackStack()
        }

        loadHistory()
    }

    private fun loadHistory() {
        when (keyword) {
            "today" -> {
                startDateTime = getCurrentDay()
                endDateTime = nextDay()
            }
            "yesterday" -> {
                startDateTime = getPreviousDays("today")
                endDateTime = getPreviousDays("yesterday")
            }
            "week" -> {
                startDateTime = getPreviousDays("today")
                endDateTime = getPreviousDays("week")
            }
            "month" -> {
                startDateTime = getPreviousDays("today")
                endDateTime = getPreviousDays("month")
            }
            "custom" -> {
                startDateTime = starDate
                endDateTime = endDate
            }
        }

        callHistory()
    }


    private fun callHistory() {

        val fromDate = startDateTime?.split("\n")?.get(0)
        val fromTime = startDateTime?.split("\n")?.get(1)

        val toDate = endDateTime?.split("\n")?.get(0)
        val toTime = endDateTime?.split("\n")?.get(1)

        commonViewModel?.getHistoryInfo(
            "en",
            localDB.getToken() ?: "",
            deviceId,
            fromDate?.replace(" ", "") ?: "",
            getNewTimeFormat(fromTime ?: ""),
            toDate?.replace(" ", "") ?: "",
            getNewTimeFormat(toTime ?: "")
        )
            ?.observe(requireActivity(), Observer { resources ->
                when (resources.status) {
                    Status.SUCCESS -> {
                        processData(resources.data!!)
                        binding. progressBar.visibility = View.INVISIBLE
                    }
                    Status.ERROR -> {
                        binding.   progressBar.visibility = View.INVISIBLE
                        Toast.makeText(
                            this@HistoryListing.context,
                            getString(R.string.no_history),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                    Status.LOADING -> TODO()
                }
            })
    }

    private fun processData(data: HistoryData) {

    }
}