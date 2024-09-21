package com.shazcom.gps.app.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.ToolsRepository
import com.shazcom.gps.app.data.response.*
import com.shazcom.gps.app.network.GPSWoxAPI
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.BaseActivity
import com.shazcom.gps.app.ui.adapter.AlertDeviceAdapter
import com.shazcom.gps.app.ui.adapter.AlertDriverAdapter
import com.shazcom.gps.app.ui.adapter.AlertGeoFenceAdapter
import com.shazcom.gps.app.ui.adapter.CustomEventsAdapter
import com.shazcom.gps.app.ui.viewmodal.ToolsViewModel
import kotlinx.android.synthetic.main.activity_alerts.*
import org.json.JSONObject
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.lang.Exception

class Alerts : BaseActivity(), KodeinAware {

    private var alertDeviceAdapter: AlertDeviceAdapter? = null
    private var alertGeoFenceAdapter: AlertGeoFenceAdapter? = null
    private var alertDriverAdapter: AlertDriverAdapter? = null

    private var customEventsAdapter: CustomEventsAdapter? = null
    override val kodein by kodein()
    private val localDB: LocalDB by instance<LocalDB>()
    private val repository: ToolsRepository by instance<ToolsRepository>()
    private var toolsViewModel: ToolsViewModel? = null
    private var typeName = "overspeed"

    private var alertData: AlertData? = null
    private var eventTypes: ArrayList<AlertTypes>? = null
    private var checkedCustomEvents = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alerts)

        alertData = intent?.getParcelableExtra("alert")

        toolBar.setNavigationOnClickListener { finish() }
        toolsViewModel = ViewModelProvider(this).get(ToolsViewModel::class.java)
        toolsViewModel?.toolsRepository = repository

        loadAlertData()

        selectAllBtn.setOnClickListener { alertDeviceAdapter?.checkAll(true) }
        deSelectAllBtn.setOnClickListener { alertDeviceAdapter?.checkAll(false) }

        selectAllEventBtn.setOnClickListener { customEventsAdapter?.checkAll(true) }
        deSelectAllEventBtn.setOnClickListener { customEventsAdapter?.checkAll(false) }

        selectAllGeoBtn.setOnClickListener { alertGeoFenceAdapter?.checkAll(true) }
        deSelectAllGeoBtn.setOnClickListener { alertGeoFenceAdapter?.checkAll(false) }

        selectAllDriverBtn.setOnClickListener { alertDriverAdapter?.checkAll(true) }
        deSelectAllDriverBtn.setOnClickListener { alertDriverAdapter?.checkAll(false) }

        saveBtn.setOnClickListener {
            addAlert()
        }
    }

    private fun checkEvent(position: Int) {
        eventTypes?.let {
            when (it[position].type) {
                "driver" -> {
                    values.visibility = View.GONE
                    eventLayout.visibility = View.GONE
                    geoFenceLayout.visibility = View.GONE
                    driverLayout.visibility = View.VISIBLE
                    typeName = "driver"
                    loadDriverAdapter(it[position].attributes?.get(0)?.options!!)
                }
                "geofence_in" -> {
                    values.visibility = View.GONE
                    eventLayout.visibility = View.GONE
                    geoFenceLayout.visibility = View.VISIBLE
                    driverLayout.visibility = View.GONE
                    typeName = "geofence_in"
                    loadDriverAdapter(it[position].attributes?.get(0)?.options!!)
                }
                "geofence_out" -> {
                    values.visibility = View.GONE
                    eventLayout.visibility = View.GONE
                    geoFenceLayout.visibility = View.VISIBLE
                    driverLayout.visibility = View.GONE
                    typeName = "geofence_out"
                    loadDriverAdapter(it[position].attributes?.get(0)?.options!!)
                }
                "geofence_inout" -> {
                    values.visibility = View.GONE
                    eventLayout.visibility = View.GONE
                    geoFenceLayout.visibility = View.VISIBLE
                    driverLayout.visibility = View.GONE
                    typeName = "geofence_inout"
                    loadDriverAdapter(it[position].attributes?.get(0)?.options!!)
                }
                "custom" -> {
                    values.visibility = View.GONE
                    eventLayout.visibility = View.VISIBLE
                    geoFenceLayout.visibility = View.GONE
                    driverLayout.visibility = View.GONE
                    typeName = "custom"

                    customEventsAdapter?.let {
                        it.setCheckItems(checkedCustomEvents)
                    }

                }
                else -> {
                    val item = it[position]
                    typeName = item.type

                    alertData?.let {
                        loadEditAlertData(it?.id!!, it?.type!!)
                    }


                    if (typeName.equals("sos", true) || typeName.equals("fuel_change", true)) {
                        values.visibility = View.GONE
                    }

                    values.visibility = View.VISIBLE
                    eventLayout.visibility = View.GONE
                    driverLayout.visibility = View.GONE
                    geoFenceLayout.visibility = View.GONE

                    item?.attributes?.let {
                        it.isNotEmpty()?.let {
                            if (it) {
                                if (item?.attributes != null) {
                                    values.hint = item?.attributes?.get(0)?.title
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadEditAlertData(id: Int, type: String) {
        toolsViewModel?.getEditAlert("en", localDB?.getToken()!!, id)
            ?.observe(this, Observer { resources ->
                when (resources.status) {
                    Status.SUCCESS -> {

                        if (type.equals("custom", true)) {
                            resources?.data?.item?.events_custom?.let {
                                checkedCustomEvents.clear()
                                for (e in it) {
                                    checkedCustomEvents.add(e.id)
                                }

                                customEventsAdapter?.let {
                                    it.setCheckItems(checkedCustomEvents)
                                }
                            }
                        } else {
                            resources?.data?.types?.forEach {

                                if (it.type == type) {
                                    it?.attributes?.let {
                                        if (it.isNotEmpty()) {
                                            values.setText("${it[0].default}")
                                        }
                                    }

                                    return@forEach
                                }
                            }
                        }
                    }
                }
            })
    }

    private fun loadGeoFenceAdapter(alertOptions: List<AlertOption>) {
        alertGeoFenceAdapter = AlertGeoFenceAdapter(alertOptions)
        geoFenceList.apply {
            layoutManager = GridLayoutManager(this@Alerts, 2)
            adapter = alertGeoFenceAdapter
        }
    }

    private fun loadDriverAdapter(alertOptions: List<AlertOption>) {
        alertDriverAdapter = AlertDriverAdapter(alertOptions)
        driverList.apply {
            layoutManager = GridLayoutManager(this@Alerts, 2)
            adapter = alertDriverAdapter
        }
    }

    private fun addAlert() {

        if (name.text.isNullOrEmpty()) {
            Toast.makeText(this@Alerts, "Please enter name to proceed", Toast.LENGTH_SHORT).show()
            return
        }

        if (alertDeviceAdapter?.getCheckedItem()?.size == 0) {
            Toast.makeText(this@Alerts, "Please select device to proceed", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (eventLayout.isVisible && customEventsAdapter?.getCheckedItem()?.size == 0) {
            Toast.makeText(this@Alerts, "Please select event to proceed", Toast.LENGTH_SHORT).show()
            return
        }

        if (geoFenceLayout.isVisible && alertGeoFenceAdapter?.getCheckedItem()?.size == 0) {
            Toast.makeText(this@Alerts, "Please select event to proceed", Toast.LENGTH_SHORT).show()
            return
        }

        if (driverLayout.isVisible && alertDriverAdapter?.getCheckedItem()?.size == 0) {
            Toast.makeText(this@Alerts, "Please select driver to proceed", Toast.LENGTH_SHORT)
                .show()
            return
        }

        var devicesArray = ""
        alertDeviceAdapter?.getCheckedItem()?.forEachIndexed { index, i ->
            if (index != (alertDeviceAdapter?.getCheckedItem()?.size!! - 1)) {
                devicesArray += "devices[]=$i"
                devicesArray += "&"
            } else {
                devicesArray += "devices[]=$i"
            }
        }

        var eventsArray = ""
        customEventsAdapter?.getCheckedItem()?.forEachIndexed { index, i ->
            if (index != (customEventsAdapter?.getCheckedItem()?.size!! - 1)) {
                eventsArray += "events_custom[]=$i"
                eventsArray += "&"
            } else {
                eventsArray += "events_custom[]=$i"
            }
        }

        var geoFenceArray = ""
        alertGeoFenceAdapter?.getCheckedItem()?.forEachIndexed { index, i ->
            if (index != (alertGeoFenceAdapter?.getCheckedItem()?.size!! - 1)) {
                geoFenceArray += "geofences[]=$i"
                geoFenceArray += "&"
            } else {
                geoFenceArray += "geofences[]=$i"
            }
        }

        var driverArray = ""
        alertDriverAdapter?.getCheckedItem()?.forEachIndexed { index, i ->
            if (index != (alertDriverAdapter?.getCheckedItem()?.size!! - 1)) {
                driverArray += "drivers[]=$i"
                driverArray += "&"
            } else {
                driverArray += "drivers[]=$i"
            }
        }

        var finalUrl = ""
        var baseUrl = "add_alert"
        alertData?.let {
            baseUrl = "edit_alert"
        }


        when (typeName) {
            "custom" -> {
                finalUrl =
                    "${GPSWoxAPI.BASE_URL}$baseUrl?lang=en&user_api_hash=${localDB.getToken()}&name=${name.text.toString()}&type=$typeName&$devicesArray&$eventsArray"
            }

            "geofence_inout" -> {
                finalUrl =
                    "${GPSWoxAPI.BASE_URL}$baseUrl?lang=en&user_api_hash=${localDB.getToken()}&name=${name.text.toString()}&type=$typeName&$devicesArray&$geoFenceArray"
            }

            "driver" -> {
                finalUrl =
                    "${GPSWoxAPI.BASE_URL}$baseUrl?lang=en&user_api_hash=${localDB.getToken()}&name=${name.text.toString()}&type=$typeName&$devicesArray&$driverArray"
            }

            "geofence_in" -> {
                finalUrl =
                    "${GPSWoxAPI.BASE_URL}$baseUrl?lang=en&user_api_hash=${localDB.getToken()}&name=${name.text.toString()}&type=$typeName&$devicesArray&$geoFenceArray"
            }

            "geofence_out" -> {
                finalUrl =
                    "${GPSWoxAPI.BASE_URL}$baseUrl?lang=en&user_api_hash=${localDB.getToken()}&name=${name.text.toString()}&type=$typeName&$devicesArray&$geoFenceArray"
            }

            else -> {
                if (!values.text.isNullOrEmpty()) {
                    finalUrl =
                        "${GPSWoxAPI.BASE_URL}$baseUrl?lang=en&user_api_hash=${localDB.getToken()}&name=${name.text.toString()}&type=$typeName&$devicesArray&$typeName=${
                            values.text.toString()
                                .trim()
                        }"
                } else {
                    Toast.makeText(this@Alerts, "Please fill the value", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }


        var notificationVal = ""
        notificationVal = if (soundNotification.isChecked) {
            "notifications[sound][active]=1"
        } else {
            "notifications[sound][active]=0"
        }

        notificationVal += if (pushNotification.isChecked) {
            "&notifications[push][active]=1"
        } else {
            "&notifications[push][active]=0"
        }


        notificationVal += if (emailNotification.isChecked) {
            "&notifications[email][active]=1"
        } else {
            "&notifications[email][active]=0"
        }


        if (emailNotification.isChecked && email.text.isNullOrEmpty()) {
            Toast.makeText(this@Alerts, "Please fill the email", Toast.LENGTH_SHORT).show()
            return
        } else {
            notificationVal += "&notifications[email][input]=${
                email.text.toString()
                    .replace(" ", "")
            }"
        }

        finalUrl += "&$notificationVal"

        if (baseUrl == "edit_alert") {
            finalUrl += "&id=${alertData?.id!!}"
        }

        toolsViewModel?.addAlert(finalUrl)?.observe(this, Observer { resources ->
            when (resources?.status) {
                Status.LOADING -> {
                    saveBtn.isEnabled = false
                }
                Status.ERROR -> {
                    Toast.makeText(
                        this@Alerts,
                        getString(R.string.something_went_wrong),
                        Toast.LENGTH_SHORT
                    ).show()
                    saveBtn.isEnabled = true
                }
                Status.SUCCESS -> {
                    saveBtn.isEnabled = true
                    if (resources.data?.status == 1) {
                        alertData?.let {
                            Toast.makeText(
                                this@Alerts,
                                "Alert Update Successfully",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        } ?: kotlin.run {
                            Toast.makeText(
                                this@Alerts,
                                "Alert Added Successfully",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }

                    }

                    val intent = Intent()
                    intent.data = Uri.parse("refresh")
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        })
    }

    private fun loadAlertData() {
        toolsViewModel?.loadAlertData("en", localDB.getToken()!!)
            ?.observe(this, Observer { resources ->
                when (resources.status) {
                    Status.ERROR -> {
                        deviceList.visibility = View.VISIBLE
                        progressView.visibility = View.INVISIBLE
                    }
                    Status.LOADING -> {
                        deviceList.visibility = View.INVISIBLE
                        progressView.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        deviceList.visibility = View.VISIBLE
                        progressView.visibility = View.INVISIBLE
                        processData(resources?.data)
                    }
                }
            })
    }

    private fun processData(data: AddAlertDataResponsee?) {

        data?.devices?.let {
            loadCustomEvents(it[0].id!!.toString())
            alertDeviceAdapter = AlertDeviceAdapter(it)
            deviceList.apply {
                layoutManager = GridLayoutManager(this@Alerts, 2)
                adapter = alertDeviceAdapter
            }
        }.also {
            try {
                /*val jObject = data?.types as ArrayList
                jObject?.let {
                    var typeData = mutableListOf<AlertTypes>()
                    for (i in 0..15) {
                        typeData.add(it[i] as AlertTypes)
                    }
                    setEventSpinner(typeData)
                }*/
                data?.types?.let {
                    setEventSpinner(it)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }.also {
            eventList?.postDelayed({
                alertData?.let { alertData ->
                    eventTypes?.forEachIndexed { index, alertType ->
                        if (alertType.type == alertData.type) {
                            loadEditDta(index, alertData)
                            return@forEachIndexed
                        }
                    }
                }
            }, 300)
        }
    }

    private fun loadEditDta(position: Int, alertData: AlertData) {

        name.setText(alertData.name)

        eventSpinner.setSelection(position, true)

        // select devices
        deviceList?.post {
            alertDeviceAdapter?.setCheckItems(alertData.devices)
        }

        geoFenceList?.post {
            alertGeoFenceAdapter?.setCheckItems(alertData.geofences)
        }

        driverList?.post {
            alertDriverAdapter?.setCheckItems(alertData.drivers)
        }

        alertData?.notifications?.let {
            it.sound?.active?.let {
                soundNotification.isChecked = it == "1"
            }

            it.push?.active?.let {
                pushNotification.isChecked = it == "1"
            }

            it.email?.active?.let {
                emailNotification.isChecked = it == "1"
            }

            it.email?.input?.let {
                email.setText(it)
            }
        }
    }

    private fun setEventSpinner(types: List<AlertTypes>?) {
        eventTypes = types as ArrayList<AlertTypes>?
        val eventAdapter = types?.let {
            ArrayAdapter(
                this@Alerts,
                android.R.layout.simple_spinner_dropdown_item,
                it
            )
        }

        eventAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        eventSpinner.adapter = eventAdapter

        eventSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                checkEvent(position)
            }
        }
    }

    private fun loadCustomEvents(device: String) {
        toolsViewModel?.getCustomEventsByDevice("en", localDB.getToken()!!, device)
            ?.observe(this, Observer { resources ->
                when (resources.status) {
                    Status.SUCCESS -> {
                        processEvents(resources?.data?.systemEvents)
                    }
                }
            })
    }

    private fun processEvents(systemEvents: SystemEvents?) {
        customEventsAdapter = CustomEventsAdapter(systemEvents?.items!!)
        eventList.apply {
            layoutManager = GridLayoutManager(this@Alerts, 2)
            adapter = customEventsAdapter
        }

        eventList?.postDelayed({
            alertData?.let {
                //loadEditDta(typeList.indexOf(it.type), it)
            }
        }, 300)
    }
}