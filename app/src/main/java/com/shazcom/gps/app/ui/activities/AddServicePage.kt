package com.shazcom.gps.app.ui.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.response.BaseResponse
import com.shazcom.gps.app.data.repository.CommonViewRepository
import com.shazcom.gps.app.data.response.ServiceData
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.BaseActivity
import com.shazcom.gps.app.ui.viewmodal.CommonViewModel
import kotlinx.android.synthetic.main.activity_add_service.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.text.SimpleDateFormat
import java.util.*

class AddServicePage : BaseActivity(), KodeinAware {

    var deviceName: String? = null
    var deviceId: Int? = null
    private var odometerValue = "0.0"
    private var engineLoadValue = "0.0"
    private var serviceData: ServiceData? = null

    override val kodein by kodein()
    private val localDB: LocalDB by instance()
    private val repository: CommonViewRepository by instance()

    private var commonViewModel: CommonViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_service)

        deviceId = intent?.extras?.getInt("deviceId", 0)
        deviceName = intent?.extras?.getString("deviceName")
        odometerValue = intent?.extras?.getString("odometer").toString()
        engineLoadValue = intent.getStringExtra("engineLoad").toString()
        serviceData = intent?.extras?.getParcelable("item")

        currentOdometer.setText(odometerValue)
        engineHrs.setText(engineLoadValue)

        setSupportActionBar(toolBar)

        toolBar.title = deviceName
        toolBar.setNavigationOnClickListener { finish() }



        commonViewModel = ViewModelProvider(this).get(CommonViewModel::class.java)
        commonViewModel?.commonViewRepository = repository

        serviceData?.let { item ->
            saveBtn.text = "Update"
            serviceName.setText(item.name)
            lastService.setText(item.last_service)
            interval.setText("${item.interval}")
            triggerEvent.setText("${item.trigger_event_left}")
            emailTxt.setText(item.email)

            renew.isChecked = item.renew_after_expiration == 1

            when (item.expiration_by) {
                "odometer" -> expirationBy.setSelection(0)
                "days" -> expirationBy.setSelection(2)
                else -> expirationBy.setSelection(1)
            }
        }

        saveBtn.setOnClickListener {
            if (serviceData != null) {
                editServiceApi()
            } else {
                addServiceApi()
            }
        }

        lastService.setOnClickListener {
            pickStartDate()
        }

        expirationBy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                lastService.isClickable = position == 2
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        serviceData?.let {
            menuInflater.inflate(R.menu.service_menu, menu)
            return true
        } ?: run {
            return false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item?.itemId == R.id.delete) {
            // action delete
            showDeleteDialog()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showDeleteDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Alert")
        alertDialog.setMessage(getString(R.string.want_to_delete_service))
        alertDialog.setPositiveButton(
            "Delete"
        ) { dialog, _ ->
            processDeleteRequest()
            dialog.dismiss()
        }

        alertDialog.setNegativeButton(
            getString(R.string.dismiss)
        ) { dialog, _ ->
            dialog.dismiss()
        }

        alertDialog.setCancelable(false)
        alertDialog.create().show()
    }

    private fun processDeleteRequest() {
        commonViewModel?.deleteService("en", localDB.getToken()!!, serviceData?.id!!)
            ?.observe(this, Observer { resources ->
                when (resources.status) {
                    Status.SUCCESS -> {
                        progressBar.visibility = View.GONE
                        processDeleteData(resources.data!!)
                    }
                    Status.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                    }
                    Status.ERROR -> {
                        progressBar.visibility = View.GONE
                    }
                }
            })
    }

    private fun addServiceApi() {

        val checkStatus = if (renew.isChecked) "1" else "0"

        commonViewModel?.addServices(
                "en",
                localDB.getToken()!!,
                deviceId!!,
                serviceName.text.toString(),
                expirationBy.selectedItem.toString(),
                interval.text.toString(),
                lastService.text.toString(),
                triggerEvent.text.toString(),
                checkStatus,
                emailTxt.text.toString()
            )
            ?.observe(this@AddServicePage, Observer { resources ->
                when (resources.status) {
                    Status.SUCCESS -> {
                        progressBar.visibility = View.GONE
                        processDeleteData(resources.data!!)
                    }
                    Status.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                    }
                    Status.ERROR -> {
                        progressBar.visibility = View.GONE
                    }
                }
            })
    }

    private fun editServiceApi() {

        val checkStatus = if (renew.isChecked) "1" else "0"

        commonViewModel?.editServices(
                "en",
                localDB.getToken()!!,
                serviceData?.id!!,
                deviceId!!,
                serviceName.text.toString(),
                expirationBy.selectedItem.toString(),
                interval.text.toString(),
                lastService.text.toString(),
                triggerEvent.text.toString(),
                checkStatus,
                emailTxt.text.toString()
            )
            ?.observe(this@AddServicePage, Observer { resources ->
                when (resources.status) {
                    Status.SUCCESS -> {
                        progressBar.visibility = View.GONE
                        saveBtn.visibility = View.VISIBLE
                        processData(resources.data!!)
                    }
                    Status.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                        saveBtn.visibility = View.INVISIBLE
                    }
                    Status.ERROR -> {
                        progressBar.visibility = View.GONE
                        saveBtn.visibility = View.VISIBLE
                        Toast.makeText(
                                this@AddServicePage,
                                getString(R.string.something_went_wrong),
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    }
                }
            })
    }

    private fun processData(baseResponse: BaseResponse) {
        if (baseResponse.status == 1) {
            Toast.makeText(this@AddServicePage, getString(R.string.service_add_successfully), Toast.LENGTH_SHORT)
                .show()
            finish()
        } else {
            Toast.makeText(this@AddServicePage, getString(R.string.valid_proper_data), Toast.LENGTH_SHORT)
                .show()
        }
    }


    private fun processDeleteData(baseResponse: BaseResponse) {
        if (baseResponse.status == 1) {
            Toast.makeText(this@AddServicePage, getString(R.string.service_delete_success), Toast.LENGTH_SHORT)
                .show()

            val intent = Intent()
            intent.data = Uri.parse("delete")
            setResult(Activity.RESULT_OK, intent)
            finish()
        } else {
            Toast.makeText(
                    this@AddServicePage,
                    "Whoops !!, something wend wrong",
                    Toast.LENGTH_SHORT
                )
                .show()
        }
    }

    private fun pickStartDate() {
        val currentDateTime = Calendar.getInstance()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)


        DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, month, day ->
                val pickedDateTime = Calendar.getInstance()
                pickedDateTime.set(year, month, day)
                val df = SimpleDateFormat("yyyy-MM-dd")
                lastService.setText(df.format(pickedDateTime.timeInMillis))
                lastService.setSelection(lastService.text.length)
            },
            startYear,
            startMonth,
            startDay
        ).show()
    }
}