package com.shazcom.gps.app.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager

import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.CommonViewRepository
import com.shazcom.gps.app.data.response.ServiceResponse
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.BaseActivity
import com.shazcom.gps.app.ui.adapter.ServiceAdapter
import com.shazcom.gps.app.ui.viewmodal.CommonViewModel
import com.mb.battery.app.utils.PaginationListener
import com.shazcom.gps.app.databinding.ActivityServicesBinding

import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class ServicePage : BaseActivity(), KodeinAware {

    private lateinit var binding: ActivityServicesBinding
    override val kodein by kodein()
    private val localDB: LocalDB by instance()
    private val repository: CommonViewRepository by instance()
    private var commonViewModel: CommonViewModel? = null
    private var serviceAdapter: ServiceAdapter? = null

    var deviceName: String? = null
    var deviceId: Int? = null
    private var odometerValue = "0.0"
    private var engineLoadValue = "0.0"

    private var currentPage = 1
    private var totalPage = 10
    private var isLoading = false

    val DELETE_REQUEST_CODE = 11230

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deviceId = intent.getIntExtra("deviceId", 0)
        deviceName = intent.getStringExtra("deviceName")
        odometerValue = intent.getStringExtra("odometer").toString()
        engineLoadValue = intent.getStringExtra("engineLoad").toString()
        with(binding) {
            toolBar.title = deviceName
            toolBar.setNavigationOnClickListener { finish() }

            commonViewModel = ViewModelProvider(this@ServicePage).get(CommonViewModel::class.java)
            commonViewModel?.commonViewRepository = repository

            loadService()

            addService.setOnClickListener {
                Intent(this@ServicePage, AddServicePage::class.java).apply {
                    putExtra("deviceId", deviceId!!)
                    putExtra("deviceName", deviceName!!)
                    putExtra("odometer", odometerValue)
                    putExtra("engineLoad", engineLoadValue)
                    startActivity(this)
                }
            }
        }
    }

    private fun loadService() = with(binding){

        commonViewModel?.getServices("en", localDB.getToken()!!, deviceId!!, currentPage)
            ?.observe(this@ServicePage, Observer { resources ->

                when (resources.status) {
                    Status.SUCCESS -> {
                        progressBar.visibility = View.GONE
                        inc.emptyText.visibility = View.INVISIBLE
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

    private fun processData(serviceData: ServiceResponse) {

        serviceData.data.isNotEmpty().let { flag ->
            if (flag) {
                if (currentPage == 1) {
                    totalPage = serviceData.last_page!!
                    serviceAdapter = ServiceAdapter(
                        this@ServicePage,
                        serviceData.data,
                        deviceName!!,
                        odometerValue,
                        engineLoadValue
                    )
                    binding.serviceList.apply {
                        layoutManager = LinearLayoutManager(this@ServicePage)
                        adapter = serviceAdapter
                    }.also {
                       binding. serviceList.addOnScrollListener(object :
                            PaginationListener(it.layoutManager as LinearLayoutManager) {
                            override fun loadMoreItems() {
                                isLoading = true
                                currentPage += 1
                                loadService()
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
                    serviceAdapter?.addNewItems(serviceData.data)
                    isLoading = false
                }
            } else {
                binding.inc.emptyText.visibility = View.VISIBLE
                binding.inc.emptyText.text = "No Service Found"
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == DELETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                loadService()
            }
        }

    }
}