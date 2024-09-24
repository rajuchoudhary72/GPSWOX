package com.shazcom.gps.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.CommonViewRepository
import com.shazcom.gps.app.data.response.*
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.BaseFragment
import com.shazcom.gps.app.ui.activities.Dashboard
import com.shazcom.gps.app.ui.viewmodal.CommonViewModel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_setup_page.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.support.closestKodein
import org.kodein.di.generic.instance

class SetUpPage : BaseFragment(), KodeinAware {

    override val kodein by closestKodein()
    private val localDB: LocalDB by instance()
    private val repository: CommonViewRepository by instance()
    private var commonViewModel: CommonViewModel? = null
    private var timezoneId: Int = 0
    private var setupData: EditUserDataResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setup_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        commonViewModel = ViewModelProvider(this).get(CommonViewModel::class.java)
        commonViewModel?.commonViewRepository = repository
        loadSettings()

        groups.setOnClickListener {
            findNavController().navigate(R.id.action_setup_to_groups)
        }

        drivers.setOnClickListener {
            findNavController().navigate(R.id.action_setup_to_driver)
        }

        events.setOnClickListener {
            findNavController().navigate(R.id.action_setup_to_events)
        }

        saveBtn.setOnClickListener {
            timezoneId =
                setupData?.timezones?.find { timezones -> timezones.value == timeZone.selectedItem.toString() }?.id!!?.let { it }!!

            val distanceUnitStr =
                setupData?.units_of_distance?.find { unitsOfDistance -> unitsOfDistance.value == distanceUnit.selectedItem.toString() }?.id?.let { it }

            val capacityUnitStr =
                setupData?.units_of_capacity?.find { unitsOfDistance -> unitsOfDistance.value == capacityUnit.selectedItem.toString() }?.id?.let { it }

            val altitudeUnitStr =
                setupData?.units_of_altitude?.find { unitsOfDistance -> unitsOfDistance.value == altitudeUnit.selectedItem.toString() }?.id?.let { it }

            commonViewModel?.editSetup(
                "en", localDB.getToken()!!,
                distanceUnitStr!!,
                capacityUnitStr!!,
                altitudeUnitStr!!,
                timezoneId,
                setupData?.sms_gateway!!,
                Gson().toJson(setupData?.groups).toString()
            )?.observe(requireActivity(), Observer {

                    resources ->
                when (resources.status) {
                    Status.ERROR -> {
                        progressView.visibility = View.GONE
                        saveBtn.visibility = View.VISIBLE
                    }
                    Status.LOADING -> {
                        progressView.visibility = View.VISIBLE
                        saveBtn.visibility = View.GONE
                    }
                    Status.SUCCESS -> {
                        progressView.visibility = View.GONE
                        saveBtn.visibility = View.VISIBLE
                    }
                }

            })
        }
    }

    private fun loadSettings() {
        commonViewModel?.getSetupData("en", localDB.getToken()!!)
            ?.observe(requireActivity(), Observer { resources ->

                when (resources.status) {
                    Status.SUCCESS -> {
                        processData(resources.data!!)
                    }
                    Status.ERROR -> {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else ->{}
                }
            })
    }

    private fun processData(data: EditUserDataResponse) {
        if (isVisible) {
            data?.let {
                populateDistanceUnit(data?.units_of_distance!!, data?.item?.unit_of_distance!!)
                populateCapacityUnit(data?.units_of_capacity!!, data?.item?.unit_of_capacity!!)
                populateAltitudeUnit(data?.units_of_altitude!!, data?.item?.unit_of_altitude!!)
                populateWeekDays(data?.weekdays!!)
                populateTimeZone(data?.timezones!!, data?.item?.timezone_id!!)
                (activity as Dashboard).saveGroups(data?.groups!!)
                setupData = data
            }
        }
    }

    private fun populateTimeZone(
        timezones: List<Timezones>,
        timezoneId: Int
    ) {
        val spinnerAdapter: ArrayAdapter<Timezones> =
            ArrayAdapter<Timezones>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                timezones
            )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeZone.adapter = spinnerAdapter

        val timeZoneVal = timezones.find { timezones -> timezones.id == timezoneId }
        timeZone.setSelection(timezones.indexOf(timeZoneVal))
    }

    private fun populateWeekDays(weekdays: List<Weekdays>) {
        val spinnerAdapter: ArrayAdapter<Weekdays> =
            ArrayAdapter<Weekdays>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                weekdays
            )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        weekDays.adapter = spinnerAdapter
    }

    private fun populateAltitudeUnit(
        unitsOfAltitude: List<Units_of_altitude>,
        unitsOfAltitude1: String
    ) {
        val spinnerAdapter: ArrayAdapter<Units_of_altitude> =
            ArrayAdapter<Units_of_altitude>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                unitsOfAltitude
            )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        altitudeUnit.adapter = spinnerAdapter
    }

    private fun populateCapacityUnit(
        unitsOfCapacity: List<Units_of_capacity>,
        unitsOfCapacity1: String
    ) {
        val spinnerAdapter: ArrayAdapter<Units_of_capacity> =
            ArrayAdapter<Units_of_capacity>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                unitsOfCapacity
            )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        capacityUnit.adapter = spinnerAdapter
    }

    private fun populateDistanceUnit(
        unitsOfDistance: List<Units_of_distance>,
        unitsOfDistance1: String
    ) {
        val spinnerAdapter: ArrayAdapter<Units_of_distance> =
            ArrayAdapter<Units_of_distance>(
                requireContext() ,
                android.R.layout.simple_spinner_item,
                unitsOfDistance
            )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        distanceUnit.adapter = spinnerAdapter

        val distanceVal = unitsOfDistance.find { distance -> distance.id == unitsOfDistance1 }
        distanceUnit.setSelection(unitsOfDistance.indexOf(distanceVal))
    }


}