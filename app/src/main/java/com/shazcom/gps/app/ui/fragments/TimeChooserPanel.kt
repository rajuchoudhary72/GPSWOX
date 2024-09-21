package com.shazcom.gps.app.ui.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.shazcom.gps.app.R
import com.shazcom.gps.app.ui.BaseFragment
import com.shazcom.gps.app.ui.activities.Dashboard
import kotlinx.android.synthetic.main.fragment__date_panel.*
import java.text.SimpleDateFormat
import java.util.*

class TimeChooserPanel : BaseFragment(), RadioGroup.OnCheckedChangeListener {

    private var deviceId: Int? = 0
    private var keyword: String = "today"

    companion object {
        fun instance() = TimeChooserPanel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment__date_panel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timeRadioGrp.setOnCheckedChangeListener(this)
        showBtn.setOnClickListener {
            val parent = parentFragment as HistoryPage

            if (keyword == "custom" && startDateTimeTxt.text == "Select Start Date" && endDateTimeTxt.text == "Select End Date"
            ) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.valid_start_end_date),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            parent.loadHistory(
                deviceId!!, keyword, startDateTimeTxt.text as String,
                endDateTimeTxt.text as String
            )
        }


        val list = (activity as Dashboard).getItemList()

        list?.let {
            val adapter = ArrayAdapter(requireContext() , R.layout.spinner_item, list)
            adapter.setDropDownViewResource(R.layout.spinner_item)
            selectDevice.adapter = adapter
            selectDevice.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    deviceId = list[position].id
                }
            }
        }

        startDateTimeTxt.setOnClickListener {
            pickDate1()
        }

        endDateTimeTxt.setOnClickListener {
            pickDate2()
        }
    }

    private fun pickDate1() {
        val currentDateTime = Calendar.getInstance()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
        val startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val startMinute = currentDateTime.get(Calendar.MINUTE)

        DatePickerDialog(
            requireContext() ,
            DatePickerDialog.OnDateSetListener { _, year, month, day ->
                TimePickerDialog(
                    this@TimeChooserPanel.context,
                    TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                        val pickedDateTime = Calendar.getInstance()
                        pickedDateTime.set(year, month, day, hour, minute)
                        val df = SimpleDateFormat("yyyy-MM-dd\nHH:mm aa")
                        startDateTimeTxt.text = df.format(pickedDateTime.timeInMillis).replace("PG", "AM").replace("PTG", "PM")
                    },
                    startHour,
                    startMinute,
                    false
                ).show()
            },
            startYear,
            startMonth,
            startDay
        ).show()
    }


    private fun pickDate2() {
        val currentDateTime = Calendar.getInstance()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
        val startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val startMinute = currentDateTime.get(Calendar.MINUTE)

        DatePickerDialog(
            requireContext() ,
            DatePickerDialog.OnDateSetListener { _, year, month, day ->
                TimePickerDialog(
                    this@TimeChooserPanel.context,
                    TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                        val pickedDateTime = Calendar.getInstance()
                        pickedDateTime.set(year, month, day, hour, minute)
                        val df = SimpleDateFormat("yyyy-MM-dd\nHH:mm aa")
                        endDateTimeTxt.text = df.format(pickedDateTime.timeInMillis).replace("PG", "AM").replace("PTG", "PM")
                    },
                    startHour,
                    startMinute,
                    false
                ).show()
            },
            startYear,
            startMonth,
            startDay
        ).show()
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        when (checkedId) {
            R.id.today -> {
                keyword = "today"
                timePanel.visibility = View.GONE
            }
            R.id.yesterDay -> {
                keyword = "yesterday"
                timePanel.visibility = View.GONE
            }
            R.id.week -> {
                keyword = "week"
                timePanel.visibility = View.GONE
            }
            R.id.currentMonth -> {
                keyword = "currentMonth"
                timePanel.visibility = View.GONE
            }
            R.id.lastMonth -> {
                keyword = "lastMonth"
                timePanel.visibility = View.GONE
            }
            R.id.customPeriod -> {
                keyword = "custom"
                timePanel.visibility = View.VISIBLE
            }
        }
    }
}