package com.shazcom.gps.app.ui.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DownloadManager
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.CommonViewRepository
import com.shazcom.gps.app.data.repository.ToolsRepository
import com.shazcom.gps.app.data.response.BaseResponse
import com.shazcom.gps.app.data.response.Items
import com.shazcom.gps.app.data.response.ReportData
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.BaseFragment
import com.shazcom.gps.app.ui.activities.Dashboard
import com.shazcom.gps.app.ui.adapter.ReportDeviceAdapter
import com.shazcom.gps.app.ui.viewmodal.CommonViewModel
import com.shazcom.gps.app.ui.viewmodal.ToolsViewModel
import com.shazcom.gps.app.utils.*
import com.shazcom.gps.app.utils.Constants.CHOOSER_PERMISSIONS_REQUEST_CODE
import com.shazcom.gps.app.utils.Constants.storagePermissions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.shazcom.gps.app.databinding.FragmentReportBinding
import com.shazcom.gps.app.network.GPSWoxAPI

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.support.closestKodein
import org.kodein.di.generic.instance
import java.io.File
import java.io.FileNotFoundException
import java.net.URL
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Reports : BaseFragment(), KodeinAware, TextWatcher {

    private lateinit var binding: FragmentReportBinding
    private var behavior: BottomSheetBehavior<*>? = null
    private var mReportFormat = "pdf"
    private var extension = ".pdf"
    private var mReportType = 1
    private var mReportTypeTxt = "General_Information"
    private var destinationPath: String = ""
    private var mUrl: String = ""
    override val kodein by closestKodein()
    private val localDB: LocalDB by instance<LocalDB>()

    private val repository: CommonViewRepository by instance<CommonViewRepository>()
    private var commonViewModel: CommonViewModel? = null

    private val toolsRepository: ToolsRepository by instance<ToolsRepository>()
    private var toolsViewModel: ToolsViewModel? = null

    private var reportDeviceAdapter: ReportDeviceAdapter? = null

    private var reportData: ReportData? = null

    private var listItems: ArrayList<Items> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        (requireActivity() as Dashboard).findViewById<ImageView>(R.id.nav_icon).visibility =
            View.VISIBLE
        (requireActivity() as Dashboard).findViewById<ImageView>(R.id.back_icon).visibility =
            View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val app = activity as Dashboard
        val items = app.getDeviceList()
        initBottomSheet()

        (requireActivity() as Dashboard).findViewById<ImageView>(R.id.nav_icon).visibility =
            View.GONE
        (requireActivity() as Dashboard).findViewById<ImageView>(R.id.back_icon).visibility =
            View.VISIBLE

        reportData = arguments?.getParcelable("report")
        reportData?.let {
            (requireActivity() as Dashboard).findViewById<TextView>(R.id.navTitle).text =
                "Edit Report"
        }

        toolsViewModel = ViewModelProvider(this).get(ToolsViewModel::class.java)
        toolsViewModel?.toolsRepository = toolsRepository

        commonViewModel = ViewModelProvider(this).get(CommonViewModel::class.java)
        commonViewModel?.commonViewRepository = repository
        with(binding) {
            startDateTxt.text = getCurrentDay().split("\n")[0]
            startTimeTxt.text = getCurrentDay().split("\n")[1]

            endDateTxt.text = nextDay().split("\n")[0]
            endTimeTxt.text = nextDayAM().split("\n")[1]

            reportDevices.setOnClickListener { view ->
                items?.let {
                    if (view.isPressed) {
                        if (behavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
                            behavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
                        } else {
                            behavior?.setState(BottomSheetBehavior.STATE_COLLAPSED)
                        }
                    }
                }
            }

            reportDeviceAdapter =
                ReportDeviceAdapter(items) { item -> onDeviceChangeListener(item) }
            reportDeviceAdapter?.deSelectAll()

            inc.deviceList.apply {
                layoutManager = LinearLayoutManager(this@Reports.context)
                adapter = reportDeviceAdapter
            }

            startDateCard.setOnClickListener { pickStartDate() }
            startTimeCard.setOnClickListener { pickStartTime() }
            endDateCard.setOnClickListener { pickEndDate() }
            endTimeCard.setOnClickListener { pickEndTime() }

            activity?.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )

            reportType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    mReportTypeTxt = parent?.selectedItem.toString()
                    mReportType = getReportTypeId(mReportTypeTxt)
                }
            }

            reportFormat.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    mReportFormat = getReportFormat(position)
                    when (mReportFormat) {
                        "pdf" -> {
                            extension = ".pdf"
                        }

                        "pdf_land" -> {
                            extension = ".pdf"
                        }

                        "xls" -> {
                            extension = ".xls"
                        }

                        "html" -> {
                            extension = ".html"
                        }
                    }
                }
            }

            reportPeriod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    updateDate(position)
                }
            }

            generateBtn.setOnClickListener {

                if (listItems.size == 0) {
                    Toast.makeText(
                        this@Reports.context,
                        getString(R.string.please_select_device),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@setOnClickListener
                }

                var deviceArray = ""
                listItems.forEachIndexed { index, items ->
                    deviceArray += items.id
                    if (index != (listItems.size - 1)) {
                        deviceArray += ","
                    }
                }

                commonViewModel?.generateReport(
                    "en",
                    localDB.getToken()!!,
                    mReportType,
                    mReportFormat,
                    deviceArray,
                    "${startDateTxt.text} ${startTimeTxt.text}",
                    "${endDateTxt.text} ${endTimeTxt.text}",
                    email.text.toString().replace(" ", "")
                )
                    ?.observe(requireActivity(), androidx.lifecycle.Observer { resources ->
                        when (resources.status) {
                            Status.SUCCESS -> {
                                progressBar.visibility = View.GONE
                                generateBtn.visibility = View.VISIBLE
                                processData(resources.data!!)
                            }

                            Status.LOADING -> {
                                progressBar.visibility = View.VISIBLE
                                generateBtn.visibility = View.GONE
                            }

                            Status.ERROR -> {
                                progressBar.visibility = View.GONE
                                generateBtn.visibility = View.VISIBLE
                            }
                        }
                    })
            }

            inc.selectCheckBox.setOnClickListener {
                reportDeviceAdapter?.let {
                    reportDeviceAdapter?.selectAll()
                }
            }

            inc.unSelectCheckBox.setOnClickListener {
                reportDeviceAdapter?.deSelectAll()
            }

            email.addTextChangedListener(this@Reports)
            scheduleReport.setOnClickListener {
                proceedScheduleReport()
            }

            reportData?.let {
                generateBtn.isEnabled = false
                enableEditMode(it)
            }
        }
    }


    private fun proceedScheduleReport()= with(binding) {

        if (listItems.size == 0) {
            Toast.makeText(
                this@Reports.context,
                getString(R.string.please_select_device),
                Toast.LENGTH_SHORT
            )
                .show()
            return@with
        }

        val dailyChecked = if (daily.isChecked) 1 else null
        val weeklyChecked = if (week.isChecked) 1 else null
        val monthlyChecked = if (month.isChecked) 1 else null

        if (timeTxt.text.isNullOrEmpty()) {
            timeTxt.setText("00:00")
        }

        if (titleTxt.text.isNullOrEmpty()) {
            Toast.makeText(
                this@Reports.context,
                getString(R.string.fill_title_before_proceed),
                Toast.LENGTH_SHORT
            ).show()
            return
        }


        var postFix = "add_report"
        reportData?.let {
            postFix = "edit_report"
        }


        var deviceArrayStr = ""
        listItems.forEachIndexed { index, items ->
            deviceArrayStr += "devices[]=${items.id}"
            if (index != listItems.size - 1) {
                deviceArrayStr += "&"
            }
        }

        var finalUrl = "${GPSWoxAPI.BASE_URL}$postFix?lang=en&user_api_hash=${localDB.getToken()}" +
                "&type=$mReportType&format=$mReportFormat&$deviceArrayStr&date_from=${startDateTxt.text} ${startTimeTxt.text}" +
                "&date_to=${endDateTxt.text} ${endTimeTxt.text}&send_to_email=${
                    email.text.toString().replace(" ", "")
                }" +
                "&show_addresses=1&daily=${dailyChecked}&expense_type=all&supplier=all" +
                "&ignition_off=0&title=${titleTxt.text}&weekly=${weeklyChecked}&monthly=${monthlyChecked}" +
                "&daily_time=${timeTxt.text}&weekly_time=${timeTxt.text}&monthly_time=${timeTxt.text}"

        reportData?.let {
            finalUrl += "&id=${it.id}"
        }

        Log.e("final Url ", "${finalUrl.trim()}")
        toolsViewModel?.saveReport(finalUrl.trim())
            ?.observe(requireActivity(), androidx.lifecycle.Observer { resources ->
                Log.e("status", "${resources.status}")
                if (isVisible) {
                    when (resources.status) {

                        Status.SUCCESS -> {
                            progressBar.visibility = View.GONE
                            generateBtn.visibility = View.VISIBLE
                            scheduleReport.isEnabled = true
                            processSaveReport(resources.data!!)
                        }

                        Status.LOADING -> {
                            progressBar.visibility = View.VISIBLE
                            generateBtn.visibility = View.GONE
                            scheduleReport.isEnabled = false
                        }

                        Status.ERROR -> {
                            scheduleReport.isEnabled = false
                            progressBar.visibility = android.view.View.GONE
                            generateBtn.visibility = android.view.View.VISIBLE
                        }


                    }
                }
            })
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CHOOSER_PERMISSIONS_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mUrl?.let {
                startDownload(it)
            }
        }
    }

    private fun processData(data: BaseResponse) {
        if (data.status == 3) {
            data.url?.let {
                mUrl = data.url.replace("\\", "")
                if (arePermissionsGranted(requireActivity()!!, storagePermissions)) {
                    startDownload(it)
                } else {
                    val builder = android.app.AlertDialog.Builder(requireActivity()!!)
                    builder.setTitle("Permission Required")
                    builder.setMessage("Storage permission is required to download file")
                    builder.setPositiveButton(
                        "Proceed"
                    ) { dialog, _ ->
                        dialog?.dismiss()
                        requestPermissions(storagePermissions, CHOOSER_PERMISSIONS_REQUEST_CODE)
                    }

                    builder.create().show()
                }
            }
        } else {
            Toast.makeText(
                this@Reports.context,
                getString(R.string.valid_proper_data),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun processSaveReport(data: BaseResponse) {
        if (data.status == 1) {
            Toast.makeText(
                this@Reports.context,
                getString(R.string.report_saved),
                Toast.LENGTH_SHORT
            ).show()

            findNavController().popBackStack()
        } else {
            Toast.makeText(
                this@Reports.context,
                getString(R.string.valid_proper_data),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun replaceUriParameter(
        uri: Uri,
        key: String,
        newValue: String
    ): Uri? {
        val params = uri.queryParameterNames
        val newUri = uri.buildUpon().clearQuery()
        for (param in params) {
            newUri.appendQueryParameter(
                param,
                if (param == key) newValue else uri.getQueryParameter(param)
            )
        }
        return newUri.build()
    }

    private fun startDownload(url: String) {

        try {

            Log.e("Download URL", url)


            var deviceArray = ""
            listItems.forEachIndexed { index, items ->

                if (index == 0) {
                    deviceArray += "${items.id}&"
                } else {
                    deviceArray += "devices[$index]=${items.id}"
                    if (index != listItems.size) {
                        deviceArray += "&"
                    }
                }
            }

            val newUrl = replaceUriParameter(Uri.parse(url), "devices[0]", deviceArray)
            Log.e("New Download URL", newUrl.toString())


            Toast.makeText(
                this@Reports.context,
                getString(R.string.start_download),
                Toast.LENGTH_SHORT
            ).show()

            val mUrl = URL(URLDecoder.decode(newUrl.toString(), "UTF-8"))
            Log.e("##", "$mUrl")
            val fileName = "${
                mReportTypeTxt.replace(
                    " ",
                    "_"
                )
            }_${binding.startDateTxt.text}-${binding.endDateTxt.text}$extension"

            destinationPath = fileName

            val downloadManager =
                activity?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(mUrl.toString())
            val request = DownloadManager.Request(uri)
            request.setTitle(fileName)
            request.setDescription("Downloading ...")
            request.setMimeType(getMimeType(extension))
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            request.setVisibleInDownloadsUi(true)
            request.allowScanningByMediaScanner()
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
            downloadManager.enqueue(request)

        } catch (ex: Exception) {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url.replace("\\", ""))
            startActivity(i)
        }
    }

    private var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.KITKAT)
        override fun onReceive(
            ctxt: Context,
            intent: Intent
        ) {

            CoroutineScope(Dispatchers.Main).launch {
                delay(1000)

                val downloadManager =
                    activity?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

                val referenceId =
                    intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (!referenceId.equals(-1)) {
                    val myDownloadQuery = DownloadManager.Query()
                    myDownloadQuery.setFilterById(referenceId)
                    val c: Cursor = downloadManager.query(myDownloadQuery)
                    if (c.moveToFirst()) {
                        val status: Int = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            val downloadFilePath: String =
                                c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                                    .replace(
                                        "file://",
                                        ""
                                    )
                            val downloadTitle: String =
                                c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE))
                            c.close()
                            Log.e(
                                "DownloadPath",
                                downloadFilePath
                            )
                            Log.e(
                                "DownloadTitle",
                                downloadTitle
                            ) // Print DownloadTitle in Logcat


                            var uri = Uri.parse(downloadFilePath)
                            val file = File(uri.path)

                            val pdfIntent = Intent(Intent.ACTION_VIEW)

                            Log.e(
                                "URI ",
                                "${uri.toString().replace("\n", "").replace("\r", "")}"
                            ) // Print URI in Logcat


                            if (file.exists()) {
                                uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    FileProvider.getUriForFile(
                                        requireActivity(),
                                        context?.packageName + ".provider",
                                        file
                                    )
                                } else {
                                    Uri.fromFile(file)
                                }

                                Log.e(
                                    "URI ",
                                    "${uri.toString().replace("\n", "").replace("\r", "")}"
                                ) // Print URI in Logcat

                                try {

                                    pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    pdfIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                                    pdfIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

                                    pdfIntent.setDataAndType(uri, getMimeType(extension))
                                    activity?.startActivity(pdfIntent)
                                } catch (ex: Exception) {
                                    Toast.makeText(
                                        context,
                                        getString(R.string.no_app_found_to_open),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    ex.printStackTrace()
                                }
                            } else {
                                throw FileNotFoundException("${file.absolutePath} not found")
                            }

                        }
                    }

                }
            }
        }
    }

    override fun onDestroy() {
        activity!!.unregisterReceiver(onComplete)
        super.onDestroy()
    }

    private fun updateDate(position: Int)= with(binding) {
        when (position) {
            0 -> { // today
                startDateTxt.text = getPreviousTime("today")
                endDateTxt.text = getPreviousTime("next_day")
            }

            1 -> { // yesterday
                startDateTxt.text = getPreviousTime("yesterday")
                endDateTxt.text = getPreviousTime("today")
            }

            2 -> { // Before 2 days
                startDateTxt.text = getPreviousTime("two_days")
                endDateTxt.text = getPreviousTime("yesterday")
            }

            3 -> { // Before 3 days
                startDateTxt.text = getPreviousTime("three_days")
                endDateTxt.text = getPreviousTime("two_days")
            }

            4 -> { // This Week
                startDateTxt.text = getPreviousTime("week_start")
                endDateTxt.text = getPreviousTime("week_end")
            }

            5 -> { // Last Week
                startDateTxt.text = getPreviousTime("two_days")
                endDateTxt.text = getPreviousTime("yesterday")
            }

            6 -> { // This Month
                startDateTxt.text = getPreviousTime("month_start")
                endDateTxt.text = getPreviousTime("month_end")
            }

            7 -> { // Last Month
                startDateTxt.text = getPreviousTime("last_month")
                endDateTxt.text = getPreviousTime("month_start")
            }
        }
    }

    private fun initBottomSheet() {

        behavior = BottomSheetBehavior.from(binding.inc.deviceBottomSheet)
        behavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        behavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

                when (behavior?.state) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {
                    }

                    BottomSheetBehavior.STATE_COLLAPSED -> {
                    }

                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }

                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }
        })


    }

    fun hideBottomSheetFromOutSide(event: MotionEvent) {
        if (behavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
            val outRect = Rect()
            binding.inc.deviceBottomSheet.getGlobalVisibleRect(outRect)
            if (!outRect.contains(
                    event.rawX.toInt(),
                    event.rawY.toInt()
                )
            ) behavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun pickStartDate() {
        val currentDateTime = Calendar.getInstance()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
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
            requireContext(),
            DatePickerDialog.OnDateSetListener { _, year, month, day ->
                val pickedDateTime = Calendar.getInstance()
                pickedDateTime.set(year, month, day)
                val df = SimpleDateFormat("yyyy-MM-dd")
                binding. endDateTxt.text = df.format(pickedDateTime.timeInMillis)
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

        val mTimePicker = TimePickerDialog(
            this@Reports.context,
            TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                val pickedDateTime = Calendar.getInstance()
                pickedDateTime.set(Calendar.HOUR_OF_DAY, selectedHour)
                pickedDateTime.set(Calendar.MINUTE, selectedMinute)
                val df = SimpleDateFormat("hh:mm a")
                binding.startTimeTxt.text =
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
            this@Reports.context,
            TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                val pickedDateTime = Calendar.getInstance()
                pickedDateTime.set(Calendar.HOUR_OF_DAY, selectedHour)
                pickedDateTime.set(Calendar.MINUTE, selectedMinute)
                val df = SimpleDateFormat("hh:mm a")
                binding. endTimeTxt.text =
                    df.format(pickedDateTime.timeInMillis).replace("PG", "AM").replace("PTG", "PM")
            },
            startHour,
            startMinute,
            false
        )

        mTimePicker.setTitle("Select End Time")
        mTimePicker.show()
    }

    private fun onDeviceChangeListener(item: Items) {

        if (item.isChecked!!) {
            if (listItems.contains(item)) {
                listItems.remove(item)
            }
            listItems.add(item)
        } else {
            listItems.remove(item)
        }

        populateDeviceView()
        reportDeviceAdapter?.notifyDataSetChanged()
    }

    private fun populateDeviceView() {
        binding.reportDevices.setText("")
        listItems.forEach {
            binding. reportDevices.text.append("${it.name}, ")
        }

        if (listItems.size == 0) {
            binding.reportDevices.text.append("SELECT DEVICE")
        }
    }

    override fun afterTextChanged(s: Editable?) {
        binding.scheduleReport.isEnabled = Patterns.EMAIL_ADDRESS.matcher(binding.email.text.toString()).matches()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    private fun enableEditMode(reportData: ReportData)= with(binding) {

        reportDeviceAdapter?.setCheckItem(reportData.devices)

        getReportPosition(reportData.type)?.let {
            Log.e("reportData type ", "${reportData.type}")
            reportType.setSelection(getReportPosition(reportData.type), true)
        }

        Log.e("reportData format ", "${reportData.format}")
        reportFormat.setSelection(getReportPosition(reportData.format), true)

        titleTxt.setText("${reportData.title}")
        email.setText("${reportData.email}")

        reportData.daily.let {
            if (it == "1") {
                daily.isChecked = true
            }
            timeTxt.setText(reportData?.daily_time)
        }

        reportData.weekly.let {
            if (it == "1") {
                week.isChecked = true
            }
            timeTxt.setText(reportData?.daily_time)
        }

        reportData.monthly.let {
            if (it == "1") {
                month.isChecked = true
            }

            timeTxt.setText(reportData.daily_time)
        }

    }
}