package com.shazcom.gps.app.ui.activities

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.CommonViewRepository
import com.shazcom.gps.app.data.response.HistoryData
import com.shazcom.gps.app.databinding.ActivityServicesBinding
import com.shazcom.gps.app.databinding.ActivitySummaryBinding
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.BaseActivity
import com.shazcom.gps.app.ui.viewmodal.CommonViewModel
import com.shazcom.gps.app.utils.getCurrentDay
import com.shazcom.gps.app.utils.getNewTimeFormat
import com.shazcom.gps.app.utils.nextDay

import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
class SummaryPage : BaseActivity(), KodeinAware {

    private lateinit var binding: ActivitySummaryBinding
    override val kodein by kodein()
    private val localDB: LocalDB by instance()
    private val repository: CommonViewRepository by instance()
    private var commonViewModel: CommonViewModel? = null

    private var deviceId: Int? = null
    private var deviceName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deviceId = intent.getIntExtra("deviceId", 0)
        deviceName = intent.getStringExtra("deviceName")
        with(binding) {
            toolBar.title = deviceName!!
            toolBar.setNavigationOnClickListener { finish() }

            commonViewModel = ViewModelProvider(this@SummaryPage).get(CommonViewModel::class.java)
            commonViewModel?.commonViewRepository = repository

            showBtn.setOnClickListener {
                loadSummary()
            }

            startDateCard.setOnClickListener { pickStartDate() }
            startTimeCard.setOnClickListener { pickStartTime() }
            endDateCard.setOnClickListener { pickEndDate() }
            endTimeCard.setOnClickListener { pickEndTime() }

            startDateTxt.text = getCurrentDay().split("\n")[0]
            startTimeTxt.text = getCurrentDay().split("\n")[1]

            endDateTxt.text = nextDay().split("\n")[0]
            endTimeTxt.text = nextDay().split("\n")[1]
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadSummary() = with(binding){

        val fromDate = startDateTxt.text.toString()
        val fromTime = startTimeTxt.text.toString()

        val toDate = endDateTxt.text.toString()
        val toTime = endTimeTxt.text.toString()

        commonViewModel?.getHistoryInfo(
            "en",
            localDB.getToken()!!,
            deviceId!!,
            fromDate.replace(" ", ""),
            getNewTimeFormat(fromTime),
            toDate.replace(" ", ""),
            getNewTimeFormat(toTime)
        )
            ?.observe(this@SummaryPage, Observer { resources ->
                when (resources.status) {
                    Status.SUCCESS -> {
                        progressBar.visibility = View.INVISIBLE
                        summaryLayout.visibility = View.VISIBLE
                        processData(resources.data!!)
                    }

                    Status.LOADING -> {
                        clearData()
                        progressBar.visibility = View.VISIBLE
                    }

                    Status.ERROR -> {
                        clearData()
                        progressBar.visibility = View.INVISIBLE
                    }
                }
            })
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
                binding.startDateTxt.text = df.format(pickedDateTime.timeInMillis)

            },
            startYear,
            startMonth,
            startDay
        ).show()
    }

    private fun pickEndDate() {
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
                binding.endDateTxt.text = df.format(pickedDateTime.timeInMillis)

            },
            startYear,
            startMonth,
            startDay
        ).show()
    }

    private fun pickStartTime() {
        val currentDateTime = Calendar.getInstance()
        val startHour = currentDateTime.get(Calendar.HOUR)
        val startMinute = currentDateTime.get(Calendar.MINUTE)

        val mTimePicker: TimePickerDialog
        mTimePicker = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                val pickedDateTime = Calendar.getInstance()
                pickedDateTime.set(Calendar.HOUR_OF_DAY, selectedHour)
                pickedDateTime.set(Calendar.MINUTE, selectedMinute)
                val df = SimpleDateFormat("hh:mm a")
               binding. startTimeTxt.text =
                    df.format(pickedDateTime.timeInMillis).replace("PG", "AM").replace("PTG", "PM")
            },
            startHour,
            startMinute,
            false
        )

        mTimePicker.setTitle("Select Start Time")
        mTimePicker.show()
    }

    private fun pickEndTime() {
        val currentDateTime = Calendar.getInstance()
        val startHour = currentDateTime.get(Calendar.HOUR)
        val startMinute = currentDateTime.get(Calendar.MINUTE)

        val mTimePicker: TimePickerDialog
        mTimePicker = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                val pickedDateTime = Calendar.getInstance()
                pickedDateTime.set(Calendar.HOUR_OF_DAY, selectedHour)
                pickedDateTime.set(Calendar.MINUTE, selectedMinute)
                val df = SimpleDateFormat("hh:mm a")
                binding.endTimeTxt.text =
                    df.format(pickedDateTime.timeInMillis).replace("PG", "AM").replace("PTG", "PM")
            },
            startHour,
            startMinute,
            false
        )

        mTimePicker.setTitle("Select End Time")
        mTimePicker.show()
    }

    private fun processData(data: HistoryData)= with(binding) {
        distanceSum.text = "${data.distance_sum}"
        topSpeed.text = "${data.top_speed}"
        moveDuration.text = "${data.move_duration}"
        stopDuration.text = "${data.stop_duration}"
        fuelCons.text = "${data.fuel_consumption ?: "0 ltr"}"
    }

    private fun clearData()= with(binding) {
        summaryLayout.visibility = View.INVISIBLE
        distanceSum.text = "-"
        topSpeed.text = "-"
        moveDuration.text = "-"
        stopDuration.text = "-"
        fuelCons.text = "-"
    }
}