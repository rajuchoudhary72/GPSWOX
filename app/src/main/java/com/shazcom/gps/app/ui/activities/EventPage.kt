package com.shazcom.gps.app.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.CommonViewRepository
import com.shazcom.gps.app.data.response.EventResponse
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.BaseActivity
import com.shazcom.gps.app.ui.adapter.EventAdapter
import com.shazcom.gps.app.ui.viewmodal.CommonViewModel
import com.mb.battery.app.utils.PaginationListener
import kotlinx.android.synthetic.main.activity_events.*
import kotlinx.android.synthetic.main.activity_events.progressBar
import kotlinx.android.synthetic.main.activity_events.toolBar
import kotlinx.android.synthetic.main.empty_layout.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class EventPage : BaseActivity(), KodeinAware {

    override val kodein by kodein()
    private val localDB: LocalDB by instance<LocalDB>()
    private val repository: CommonViewRepository by instance<CommonViewRepository>()
    private var commonViewModel: CommonViewModel? = null

    private var deviceId: Int? = null
    private var deviceName: String? = null
    private var currentPage = 1
    private var totalPage = 10
    private var isLoading = false
    private var eventAdapter: EventAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)

        deviceId = intent.getIntExtra("deviceId", 0)
        deviceName = intent.getStringExtra("deviceName")

        toolBar.title = deviceName!!
        toolBar.setNavigationOnClickListener {

            if (!isTaskRoot) {
                finish()
            } else {
                Intent(this@EventPage, LoginActivity::class.java).apply {
                    startActivity(this)
                    finish()
                }
            }
        }

        commonViewModel = ViewModelProvider(this).get(CommonViewModel::class.java)
        commonViewModel?.commonViewRepository = repository

        loadEvents()
    }

    private fun loadEvents() {
        Log.e("#Currrent Page ", "$currentPage")
        commonViewModel?.getEvents("en", localDB.getToken()!!, deviceId!!, currentPage)
            ?.observe(this@EventPage, Observer { resources ->

                when (resources.status) {
                    Status.SUCCESS -> {
                        progressBar.visibility = View.GONE
                        loadMoreLayout.visibility = View.GONE
                        processData(resources.data!!)
                    }
                    Status.LOADING -> {
                        if (currentPage == 1) {
                            progressBar.visibility = View.VISIBLE
                        } else {
                            loadMoreLayout.visibility = View.VISIBLE
                        }
                    }
                    Status.ERROR -> {
                        progressBar.visibility = View.GONE
                        loadMoreLayout.visibility = View.GONE
                    }
                }
            })
    }

    private fun processData(data: EventResponse) {
        data.items.data.isNotEmpty().let { flag ->
            if (flag) {
                if (currentPage == 1) {
                    totalPage = data.items.last_page!!
                    eventAdapter = EventAdapter(data.items.data)
                    eventList.apply {
                        layoutManager = LinearLayoutManager(this@EventPage)
                        adapter = eventAdapter
                    }.also {
                        eventList.addOnScrollListener(object :
                            PaginationListener(it.layoutManager as LinearLayoutManager) {
                            override fun loadMoreItems() {
                                isLoading = true
                                currentPage += 1
                                loadEvents()
                            }

                            override fun isLastPage(): Boolean {
                                return currentPage >= totalPage
                            }

                            override fun isLoading(): Boolean {
                                return isLoading
                            }
                        })
                    }
                } else {
                    eventAdapter?.addNewItems(data.items.data)
                    isLoading = false
                }
            } else {
                emptyText.visibility = View.VISIBLE
                emptyText.text = "No Events Found"
            }
        }
    }
}