package com.shazcom.gps.app.ui.dialogs

import android.app.Dialog
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
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.fragments.DriverPage
import com.shazcom.gps.app.ui.viewmodal.ToolsViewModel
import kotlinx.android.synthetic.main.add_driver.*
import kotlinx.android.synthetic.main.add_task.closeBtn
import kotlinx.android.synthetic.main.add_task.saveBtn
import org.kodein.di.KodeinAware
import org.kodein.di.android.support.closestKodein
import org.kodein.di.generic.instance

class AddDriverDialog(private val driverPage: DriverPage) : DialogFragment(), KodeinAware {

    override val kodein by closestKodein()
    private val localDB: LocalDB by instance()
    private val repository: ToolsRepository by instance()
    private var toolsViewModel: ToolsViewModel? = null
    private val listItem = arrayListOf<Items>()
    private var device: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        toolsViewModel = ViewModelProvider(this).get(ToolsViewModel::class.java)
        toolsViewModel?.toolsRepository = repository

        val deviceData = (activity?.application as GPSWoxApp).getDeviceList()
        deviceData?.let {

            for (data in it) {
                if(data.items?.isNotEmpty() == true) {
                    listItem.addAll(data.items!!)
                }
            }

            setDevices(listItem)
        }

        closeBtn.setOnClickListener {
            dismiss()
        }

        saveBtn.setOnClickListener {
            toolsViewModel?.addDriver(
                "en",
                localDB.getToken()!!,
                device,
                name.text.toString(),
                rfid.text.toString(),
                phone.text.toString(),
                email.text.toString(),
                description.text.toString()
            )?.observe(requireActivity(), Observer { resources ->
                when (resources.status) {
                    Status.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                        saveBtn.visibility = View.INVISIBLE
                    }
                    Status.ERROR -> {
                        progressBar.visibility = View.INVISIBLE
                        saveBtn.visibility = View.VISIBLE
                        Toast.makeText(
                            this@AddDriverDialog.context,
                            getString(R.string.valid_proper_data),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Status.SUCCESS -> {
                        progressBar.visibility = View.INVISIBLE
                        saveBtn.visibility = View.VISIBLE
                        processData(resources.data!!)
                    }
                }
            })
        }
    }

    private fun processData(data: BaseResponse) {
        data?.let {
            if (data.status == 1) {
                driverPage.loadDrivers()
                dismiss()
            } else {
                Toast.makeText(
                    this@AddDriverDialog.context,
                    getString(R.string.valid_proper_data),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(activity!!, R.style.customDialogTheme)
        dialog.setContentView(R.layout.add_driver)
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

    private fun setDevices(list: List<Items>) {
        val deviceAdapter = ArrayAdapter<Items>(
            requireContext() ,
            android.R.layout.simple_spinner_dropdown_item,
            list
        )
        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deviceSpinner.adapter = deviceAdapter

        deviceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_driver, container, false)
    }


}