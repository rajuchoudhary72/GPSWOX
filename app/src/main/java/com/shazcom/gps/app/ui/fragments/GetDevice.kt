package com.shazcom.gps.app.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.shazcom.gps.app.GPSWoxApp
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.CommonViewRepository
import com.shazcom.gps.app.data.response.DeviceData
import com.shazcom.gps.app.data.response.Items
import com.shazcom.gps.app.databinding.ActivityDeviceBinding
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.BaseFragment
import com.shazcom.gps.app.ui.activities.Dashboard
import com.shazcom.gps.app.ui.activities.DeviceMapCluster
import com.shazcom.gps.app.ui.adapter.DeviceAdapter
import com.shazcom.gps.app.ui.viewmodal.CommonViewModel

import org.kodein.di.KodeinAware
import org.kodein.di.android.support.closestKodein
import org.kodein.di.generic.instance

class GetDevice : BaseFragment(), KodeinAware, TextWatcher {

    private lateinit var binding: ActivityDeviceBinding
    private var deviceAdapter: DeviceAdapter? = null
    override val kodein by closestKodein()
    private val localDB: LocalDB by instance<LocalDB>()
    private val repository: CommonViewRepository by instance<CommonViewRepository>()
    private var commonViewModel: CommonViewModel? = null
    private val deviceHandler = Handler()

    private var app: GPSWoxApp? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityDeviceBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun openMapCluster(type: String) {
        Intent(requireContext(), DeviceMapCluster::class.java).apply {
            putExtra("type", type)
            startActivity(this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app = requireActivity()?.application as GPSWoxApp
        commonViewModel = ViewModelProvider(this).get(CommonViewModel::class.java)
        commonViewModel?.commonViewRepository = repository
        with(binding) {
            swipeRefreshLayout.setOnRefreshListener {
                loadData()
            }

            requireActivity().findViewById<ImageView>(R.id.search_icon)?.setOnClickListener {
                if (searchEdt.isVisible) {
                    searchEdt.setText("")
                    searchEdt.visibility = View.GONE
                    topIndication.visibility = View.VISIBLE
                    radioGroup.visibility = View.VISIBLE
                    topTxtLayout.visibility = View.VISIBLE
                } else {
                    searchEdt.setText("")
                    searchEdt.visibility = View.VISIBLE
                    topIndication.visibility = View.GONE
                    radioGroup.visibility = View.GONE
                    topTxtLayout.visibility = View.GONE
                }
            }

            searchEdt.addTextChangedListener(this@GetDevice)
            loadData()

            allTabLayout.setOnClickListener {
                if (allTab.isChecked) {
                    app?.getDeviceList()?.let {
                        openMapCluster("all")
                    }
                } else {
                    allTab.isChecked = true
                    deviceAdapter?.filter?.filter("")
                }
            }

            runningTabLayout.setOnClickListener {
                if (runningTab.isChecked) {
                    app?.getDeviceList()?.let {
                        openMapCluster("green")
                    }
                } else {
                    runningTab.isChecked = true
                    deviceAdapter?.filter?.filter("green")
                    deviceAdapter?.notifyDataSetChanged()
                }
            }


            idleTabLayout.setOnClickListener {
                if (idleTab.isChecked) {
                    app?.getDeviceList()?.let {
                        openMapCluster("yellow")
                    }
                } else {
                    idleTab.isChecked = true
                    deviceAdapter?.filter?.filter("yellow")
                }
            }

            inactiveTabLayout.setOnClickListener {
                if (inActiveTab.isChecked) {
                    app?.getDeviceList()?.let {
                        openMapCluster("blue")
                    }
                } else {
                    inActiveTab.isChecked = true
                    deviceAdapter?.filter?.filter("blue")
                }
            }

            stopTabLayout.setOnClickListener {
                if (stopTab.isChecked) {
                    app?.getDeviceList()?.let {
                        openMapCluster("red")
                    }
                } else {
                    stopTab.isChecked = true
                    deviceAdapter?.filter?.filter("red")
                }
            }

            (activity as Dashboard)?.let {
                it.loadGeofence()
                it.loadPOIMarkers()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        deviceHandler.postDelayed(uiRefresher, 12000)
    }

    override fun onStop() {
        super.onStop()
        deviceHandler.removeCallbacks(uiRefresher)
    }

    private val uiRefresher: Runnable = object : Runnable {
        override fun run() {
            if (!binding.searchEdt.isVisible) {
                loadData()
            }
            deviceHandler.postDelayed(this, 12000)
        }
    }

    private fun loadData() {

        commonViewModel?.getDeviceInfo("en", localDB.getToken()!!)
            ?.observe(requireActivity(), Observer { resources ->

                if (isVisible) {
                    when (resources.status) {
                        Status.SUCCESS -> {
                            binding.swipeRefreshLayout.isRefreshing = false
                            binding.progressBar.visibility = View.GONE
                            processData(resources.data!!)
                            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        }

                        Status.LOADING -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }

                        Status.ERROR -> {
                            binding.swipeRefreshLayout.isRefreshing = false
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                "${resources.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                        }
                    }
                }
            })
    }

    private fun processData(data: List<DeviceData>) {
        if (data.isNotEmpty()) {

            var redCount = 0
            var greenCount = 0
            var blueCount = 0
            var yellowCount = 0

            val routeItemList = arrayListOf<Items>()
            data.forEach { deviceData ->
                deviceData.items.forEach { item ->
                    routeItemList.add(item)
                    when (item.icon_color) {
                        "red" -> {
                            redCount += 1
                        }

                        "yellow" -> {
                            yellowCount += 1
                        }

                        "blue" -> {
                            blueCount += 1
                        }

                        "green" -> {
                            greenCount += 1
                        }
                    }
                }
            }

            (activity as Dashboard).saveAllDevices(data)
            (activity?.application as GPSWoxApp).saveAllDevices(data)

            if (routeItemList.size > 0) {
                (activity as Dashboard).saveDevices(routeItemList)
            }


            deviceAdapter?.let {
                deviceAdapter?.updateItems(data)
                val filterTxt =
                    binding.radioController.findViewById<RadioButton>(binding.radioController.checkedRadioButtonId).tag.toString()
                binding.deviceList?.postDelayed({ deviceAdapter?.filter?.filter(filterTxt) }, 100)

            } ?: kotlin.run {
                deviceAdapter = DeviceAdapter(data)

                binding.deviceList.apply {
                    layoutManager = LinearLayoutManager(this@GetDevice.context)
                    adapter = deviceAdapter
                }

                (binding.deviceList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }

            binding.inc.emptyText.visibility = View.GONE
            binding.runningTxt.text = "$greenCount"
            binding.idleTxt.text = "$yellowCount"
            binding.inActiveTxt.text = "$blueCount"
            binding. stopTxt.text = "$redCount"

            binding.totalTxt.text = (greenCount + yellowCount + blueCount + redCount).toString()
            binding. deviceList.visibility = View.VISIBLE

        } else {
            binding.inc.emptyText.visibility = View.VISIBLE
        }
    }

    override fun afterTextChanged(s: Editable?) {
        deviceAdapter?.filter?.filter(s.toString())
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

}