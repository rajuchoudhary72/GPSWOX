package com.shazcom.gps.app.ui.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.ToolsRepository
import com.shazcom.gps.app.data.response.DeviceCommandResponse
import com.shazcom.gps.app.data.response.DeviceGprs
import com.shazcom.gps.app.data.response.SendCommandDataResponse
import com.shazcom.gps.app.data.response.TemplateData
import com.shazcom.gps.app.network.GPSWoxAPI
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.BaseActivity
import com.shazcom.gps.app.ui.viewmodal.ToolsViewModel
import com.shazcom.gps.app.utils.getCommandList
import kotlinx.android.synthetic.main.activity_gprs_command.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class GprsCommand : BaseActivity(), KodeinAware {

    private var type: String = "custom"
    private var deviceData: List<DeviceGprs>? = null
    override val kodein by kodein()
    private val localDB: LocalDB by instance<LocalDB>()
    private val repository: ToolsRepository by instance<ToolsRepository>()
    private var toolsViewModel: ToolsViewModel? = null
    private var device = 0
    private var deviceSMS = 0
    private var templateId = 0
    private var viewSwitch: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gprs_command)

        toolBar.setNavigationOnClickListener { finish() }

        toolsViewModel = ViewModelProvider(this).get(ToolsViewModel::class.java)
        toolsViewModel?.toolsRepository = repository
        loadCommandData()

        gprsBtn.setOnClickListener {
            gprsBtn.isChecked = true
            smsBtn.isChecked = false
            if (viewSwitch) {
                viewSwitcher.showNext()
                viewSwitch = false
            }
        }

        smsBtn.setOnClickListener {
            gprsBtn.isChecked = false
            smsBtn.isChecked = true
            if (!viewSwitch) {
                viewSwitcher.showPrevious()
                viewSwitch = true
            }
        }

        sendCommandBtn.setOnClickListener {
            sendCommand()
        }

        sendCommandBtnSMS.setOnClickListener {
            sendSmsCommand()
        }


        deviceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.e("onItem Selected", "onNothingSelected")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Log.e("onItem Selected", "onItemSelected")
                deviceData?.let {
                    device = it?.get(position)?.id!!
                    loadDeviceCommand(device)
                }

            }
        }
    }

    private fun loadCommandData() {
        toolsViewModel?.loadCommandData("en", localDB?.getToken()!!)
            ?.observe(this, Observer { resources ->
                when (resources.status) {
                    Status.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                        sendCommandBtn.visibility = View.INVISIBLE
                    }
                    Status.ERROR -> {
                        progressBar.visibility = View.INVISIBLE
                        sendCommandBtn.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        processData(resources?.data)
                        progressBar.visibility = View.INVISIBLE
                        sendCommandBtn.visibility = View.VISIBLE
                    }
                }
            })
    }

    private fun processData(data: SendCommandDataResponse?) {

        if (data?.devices_gprs?.isNotEmpty()!!) {
            deviceData = data?.devices_gprs
            device = data?.devices_gprs?.get(0)?.id!!
            loadDeviceCommand(device)

            val deviceAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                data?.devices_gprs
            )
            deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            deviceSpinner.adapter = deviceAdapter
            deviceSpinnerSMS.adapter = deviceAdapter



            deviceSpinnerSMS.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    deviceSMS = data?.devices_gprs?.get(position)?.id!!
                }

            }

            setSmsTemplate(data?.sms_templates)
        }
    }

    private fun loadDeviceCommand(device: Int) {
        toolsViewModel?.loadDeviceCommandData("en", localDB?.getToken()!!, device)
            ?.observe(this, Observer { resources ->
                when (resources.status) {
                    Status.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                        sendCommandBtn.visibility = View.INVISIBLE
                    }
                    Status.ERROR -> {
                        progressBar.visibility = View.INVISIBLE
                        sendCommandBtn.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        processDeviceData(resources?.data)
                        progressBar.visibility = View.INVISIBLE
                        sendCommandBtn.visibility = View.VISIBLE
                    }
                }
            })
    }

    private fun processDeviceData(data: List<DeviceCommandResponse>?) {
        data?.let {
            setCommandSpinner(it)
        }
    }

    private fun setCommandSpinner(list: List<DeviceCommandResponse>) {
        val commandAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            list
        )
        commandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        commandSpinner.adapter = commandAdapter

        commandSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val gprsVal = list[position]
                type = gprsVal.type
                gprsVal?.attributes?.let {
                    if (it.isNotEmpty()) {
                        if(!it[0].default.isNullOrEmpty()) {
                            messageTxt.setText("${it[0].default}")
                            messageTxt.setSelection(messageTxt.length())
                        }
                    }
                }

            }
        }
    }

    private fun setSmsTemplate(list: List<TemplateData>) {
        val smsAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            list
        )
        smsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        templateSpinner.adapter = smsAdapter

        templateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val smsVal = list[position]
                templateId = smsVal.id
                smsVal.message?.let {
                    messageTxtSMS.setText("${it}")
                    messageTxtSMS.setSelection(messageTxtSMS.length())
                }
            }
        }
    }

    private fun sendCommand() {

        toolsViewModel?.sendCommand(
            "en",
            localDB?.getToken()!!,
            device,
            type!!,
            messageTxt.text.toString()
        )
            ?.observe(this, Observer { resources ->
                when (resources.status) {
                    Status.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                        sendCommandBtn.visibility = View.INVISIBLE
                    }
                    Status.ERROR -> {
                        progressBar.visibility = View.INVISIBLE
                        sendCommandBtn.visibility = View.VISIBLE
                        Toast.makeText(this, getString(R.string.whoops_no_gprs), Toast.LENGTH_SHORT)
                            .show()
                    }
                    Status.SUCCESS -> {
                        progressBar.visibility = View.INVISIBLE
                        sendCommandBtn.visibility = View.VISIBLE
                        if(resources?.data?.error != null) {
                            Toast.makeText(this, getString(R.string.whoops_no_gprs), Toast.LENGTH_SHORT)
                                .show()
                        }else {
                            Toast.makeText(
                                this,
                                getString(R.string.command_send_success),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            })
    }

    private fun sendSmsCommand() {

        var smsUrl = GPSWoxAPI.BASE_URL + "send_sms_command?"
        smsUrl += "lang=en&user_api_hash=${localDB.getToken()}"
        smsUrl += "&devices[]=$deviceSMS"
        smsUrl += "&sms_template_id=$templateId"
        smsUrl += "&message=${messageTxtSMS.text.toString()}"

        toolsViewModel?.sendSmsCommand(
            smsUrl
        )
            ?.observe(this, Observer { resources ->
                when (resources.status) {
                    Status.LOADING -> {
                        progressBarSMS.visibility = View.VISIBLE
                        sendCommandBtnSMS.visibility = View.INVISIBLE
                    }
                    Status.ERROR -> {
                        progressBarSMS.visibility = View.INVISIBLE
                        sendCommandBtnSMS.visibility = View.VISIBLE
                        Toast.makeText(
                            this,
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    Status.SUCCESS -> {
                        progressBarSMS.visibility = View.INVISIBLE
                        sendCommandBtnSMS.visibility = View.VISIBLE
                        Toast.makeText(
                            this,
                            getString(R.string.command_send_success),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
    }
}
