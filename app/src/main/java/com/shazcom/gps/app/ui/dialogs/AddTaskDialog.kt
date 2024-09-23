package com.shazcom.gps.app.ui.dialogs

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.shazcom.gps.app.GPSWoxApp
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.ToolsRepository
import com.shazcom.gps.app.data.response.BaseResponse
import com.shazcom.gps.app.data.response.Items
import com.shazcom.gps.app.data.vo.MapData
import com.shazcom.gps.app.databinding.AddTaskBinding
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.activities.TaskPage
import com.shazcom.gps.app.ui.viewmodal.ToolsViewModel
import kotlinx.coroutines.flow.combine

import org.kodein.di.KodeinAware
import org.kodein.di.android.support.closestKodein
import org.kodein.di.generic.instance
import java.text.SimpleDateFormat
import java.util.*


class AddTaskDialog(private val taskPage: TaskPage) : DialogFragment(), KodeinAware {

    private lateinit var binding: AddTaskBinding
    override val kodein by closestKodein()
    private val localDB: LocalDB by instance()
    private val repository: ToolsRepository by instance()
    private var toolsViewModel: ToolsViewModel? = null

    private var device: Int = 0
    private var priority: Int = 1

    private var pickUpAddress: String = ""
    private var pickUpLat: Double = 0.0
    private var pickUpLng: Double = 0.0
    private var pickupTimeFrom = ""
    private var pickupTimeTo = ""

    private var deliveryAddress: String = ""
    private var deliveryLat: Double = 0.0
    private var deliveryLng: Double = 0.0
    private var deliveryTimeFrom = ""
    private var deliveryTimeTo = ""
    val listItem = arrayListOf<Items>()

    private var locationPickerDialog: LocationPickerDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        toolsViewModel = ViewModelProvider(this).get(ToolsViewModel::class.java)
        toolsViewModel?.toolsRepository = repository

        val deviceData = (activity?.application as GPSWoxApp).getDeviceList()
        deviceData?.let {

            for (data in it) {
                listItem.addAll(data.items)
            }

            setDevices(listItem)
        }

        binding.pickupAddress.setOnClickListener {
            locationPickerDialog = LocationPickerDialog(true, this)
            locationPickerDialog?.show(childFragmentManager, LocationPickerDialog::class.java.name)
        }

        binding.deliverAddress.setOnClickListener {
            locationPickerDialog = LocationPickerDialog(false, this)
            locationPickerDialog?.show(childFragmentManager, LocationPickerDialog::class.java.name)
        }


        binding.closeBtn.setOnClickListener {
            dismiss()
        }

        binding.saveBtn.setOnClickListener {
            toolsViewModel?.addTask(
                "en", localDB.getToken()!!,
                device,
                binding. title.text.toString(),
                binding.  comment.text.toString(),
                priority,
                1,
                pickUpAddress,
                pickUpLat,
                pickUpLng,
                pickupTimeFrom,
                pickupTimeTo,
                deliveryAddress,
                deliveryLat,
                deliveryLng,
                deliveryTimeFrom,
                deliveryTimeTo
            )?.observe(requireActivity(), Observer { resources ->
                when (resources.status) {
                    Status.LOADING -> {
                        binding.  progressBar.visibility = View.VISIBLE
                        binding.  saveBtn.visibility = View.INVISIBLE
                    }
                    Status.ERROR -> {
                        binding. progressBar.visibility = View.INVISIBLE
                        binding. saveBtn.visibility = View.VISIBLE
                        Toast.makeText(
                            this@AddTaskDialog.context,
                            getString(R.string.valid_proper_data),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Status.SUCCESS -> {
                        binding.progressBar.visibility = View.INVISIBLE
                        binding. saveBtn.visibility = View.VISIBLE
                        processData(resources.data!!)
                    }
                }
            })
        }

        binding. pickFrom.setOnClickListener { pickDateAndTime("pick_from") }
        binding. pickTo.setOnClickListener { pickDateAndTime("pick_to") }
        binding.delFrom.setOnClickListener { pickDateAndTime("del_from") }
        binding. delTo.setOnClickListener { pickDateAndTime("del_to") }
    }

    private fun setDevices(list: List<Items>) {
        val deviceAdapter = ArrayAdapter<Items>(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            list
        )
        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.deviceSpinner.adapter = deviceAdapter

        binding.deviceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                device = listItem[position].id!!
            }

        }
    }


    private fun processData(data: BaseResponse) {
        data?.let {
            if (data.status == 1) {
                taskPage.loadTaskList()
                dismiss()
            } else {
                Toast.makeText(
                    this@AddTaskDialog.context,
                    getString(R.string.valid_proper_data),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(activity!!, R.style.customDialogTheme)
        dialog.setContentView(R.layout.add_task)
        return dialog
    }

    override fun onStart() {
        super.onStart()

        if (dialog != null) {
            dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                dialog?.window?.statusBarColor =
                    ContextCompat.getColor(context!!, R.color.colorPrimaryDark)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=AddTaskBinding.inflate(inflater,container, false)
        return binding.root
    }

    fun saveMapData(mapData: MapData) {
        if (mapData.isPickUp) {
            pickUpAddress = mapData.address
            pickUpLat = mapData.lat
            pickUpLng = mapData.lng

            binding. pickupAddress.text = pickUpAddress

        } else {
            deliveryAddress = mapData.address
            deliveryLat = mapData.lat
            deliveryLng = mapData.lng

            binding. deliverAddress.text = deliveryAddress
        }
    }


    private fun pickDateAndTime(tag: String) {
        val currentDateTime = Calendar.getInstance()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
        val startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val startMinute = currentDateTime.get(Calendar.MINUTE)

        DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _, year, month, day ->
                TimePickerDialog(
                    this@AddTaskDialog.context,
                    TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                        val pickedDateTime = Calendar.getInstance()
                        pickedDateTime.set(year, month, day)
                        val df = SimpleDateFormat("yyyy-MM-dd HH:mm aa")
                        setDateAndTime(tag, df.format(pickedDateTime.timeInMillis).replace("PG", "AM").replace("PTG", "PM"))
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

    private fun setDateAndTime(tag: String, format: String) {
        when (tag) {
            "pick_from" -> {
                binding.  pickFrom.text = format
                pickupTimeFrom = format
            }
            "pick_to" -> {
                binding. pickTo.text = format
                pickupTimeTo = format
            }
            "del_from" -> {
                binding. delFrom.text = format
                deliveryTimeFrom = format
            }
            "del_to" -> {
                binding. delTo.text = format
                deliveryTimeTo = format
            }
        }
    }
}