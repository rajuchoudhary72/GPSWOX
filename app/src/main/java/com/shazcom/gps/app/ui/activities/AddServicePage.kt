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
import com.shazcom.gps.app.databinding.ActivityAddServiceBinding
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.BaseActivity
import com.shazcom.gps.app.ui.viewmodal.CommonViewModel

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

    private lateinit var binding: ActivityAddServiceBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deviceId = intent?.extras?.getInt("deviceId", 0)
        deviceName = intent?.extras?.getString("deviceName")
        odometerValue = intent?.extras?.getString("odometer").toString()
        engineLoadValue = intent.getStringExtra("engineLoad").toString()
        serviceData = intent?.extras?.getParcelable("item")

        binding.currentOdometer.setText(odometerValue)
        binding.engineHrs.setText(engineLoadValue)

        setSupportActionBar(binding.toolBar)

        binding.toolBar.title = deviceName
        binding.toolBar.setNavigationOnClickListener { finish() }



        commonViewModel = ViewModelProvider(this).get(CommonViewModel::class.java)
        commonViewModel?.commonViewRepository = repository

        serviceData?.let { item ->
            binding.saveBtn.text = "Update"
            binding.serviceName.setText(item.name)
            binding.lastService.setText(item.last_service)
            binding.interval.setText("${item.interval}")
            binding.triggerEvent.setText("${item.trigger_event_left}")
            binding.emailTxt.setText(item.email)

            binding.renew.isChecked = item.renew_after_expiration == 1

            when (item.expiration_by) {
                "odometer" -> binding.expirationBy.setSelection(0)
                "days" -> binding.expirationBy.setSelection(2)
                else -> binding.expirationBy.setSelection(1)
            }
        }

        binding.saveBtn.setOnClickListener {
            if (serviceData != null) {
                editServiceApi()
            } else {
                addServiceApi()
            }
        }

        binding.lastService.setOnClickListener {
            pickStartDate()
        }

        binding.expirationBy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                binding.lastService.isClickable = position == 2
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
                        binding.progressBar.visibility = View.GONE
                        processDeleteData(resources.data!!)
                    }
                    Status.LOADING -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    Status.ERROR -> {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            })
    }

    private fun addServiceApi() {

        val checkStatus = if (binding.renew.isChecked) "1" else "0"

        commonViewModel?.addServices(
                "en",
                localDB.getToken()!!,
                deviceId!!,
            binding.serviceName.text.toString(),
            binding. expirationBy.selectedItem.toString(),
            binding. interval.text.toString(),
            binding.  lastService.text.toString(),
            binding.   triggerEvent.text.toString(),
                checkStatus,
            binding.   emailTxt.text.toString()
            )
            ?.observe(this@AddServicePage, Observer { resources ->
                when (resources.status) {
                    Status.SUCCESS -> {
                        binding. progressBar.visibility = View.GONE
                        processDeleteData(resources.data!!)
                    }
                    Status.LOADING -> {
                        binding. progressBar.visibility = View.VISIBLE
                    }
                    Status.ERROR -> {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            })
    }

    private fun editServiceApi() {

        val checkStatus = if (binding.renew.isChecked) "1" else "0"

        commonViewModel?.editServices(
                "en",
                localDB.getToken()!!,
                serviceData?.id!!,
                deviceId!!,
            binding. serviceName.text.toString(),
            binding.expirationBy.selectedItem.toString(),
            binding.interval.text.toString(),
            binding.lastService.text.toString(),
            binding.triggerEvent.text.toString(),
                checkStatus,
            binding. emailTxt.text.toString()
            )
            ?.observe(this@AddServicePage, Observer { resources ->
                when (resources.status) {
                    Status.SUCCESS -> {
                        binding.progressBar.visibility = View.GONE
                        binding.saveBtn.visibility = View.VISIBLE
                        processData(resources.data!!)
                    }
                    Status.LOADING -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding. saveBtn.visibility = View.INVISIBLE
                    }
                    Status.ERROR -> {
                        binding.progressBar.visibility = View.GONE
                        binding.saveBtn.visibility = View.VISIBLE
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
                binding.lastService.setText(df.format(pickedDateTime.timeInMillis))
                binding.lastService.setSelection(binding.lastService.text.length)
            },
            startYear,
            startMonth,
            startDay
        ).show()
    }
}