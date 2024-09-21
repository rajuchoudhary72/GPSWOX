package com.shazcom.gps.app.utils

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.google.android.libraries.maps.GoogleMap
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.TemplateData
import java.lang.Integer.parseInt
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


object Constants {
    const val SUCCESS_RESULT = 0
    const val FAILURE_RESULT = 1
    private const val PACKAGE_NAME = "com.shazcom.gps.app"
    const val RECEIVER = "$PACKAGE_NAME.RECEIVER"
    const val KEYWORD = "$PACKAGE_NAME.KEYWORD"
    const val RESULT_DATA_KEY = "$PACKAGE_NAME.RESULT_DATA_KEY"
    const val LOCATION_DATA_EXTRA = "$PACKAGE_NAME.LOCATION_DATA_EXTRA"
    val storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val CHOOSER_PERMISSIONS_REQUEST_CODE = 7557
}

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    connectivityManager.activeNetworkInfo.also {
        return it != null && it.isConnected
    }
}

fun View.hideKeyboard() {
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

fun expand(v: View) {

    v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    val targetHeight = v.measuredHeight

    v.layoutParams.height = 1
    v.visibility = View.VISIBLE

    val va = ValueAnimator.ofInt(1, targetHeight)
    va.addUpdateListener { animation ->
        v.layoutParams.height = animation.animatedValue as Int
        v.requestLayout()
    }
    va.addListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator) {
        }

        override fun onAnimationEnd(animation: Animator) {
            v.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }

        override fun onAnimationCancel(animation: Animator) {
        }

        override fun onAnimationStart(animation: Animator) {
        }

    })
    va.duration = 300
    va.interpolator = OvershootInterpolator()
    va.start()
}

fun collapse(v: View) {
    val initialHeight = v.measuredHeight
    val va = ValueAnimator
        .ofInt(initialHeight, 0)
        .setDuration(300)

    va?.addUpdateListener {

        // get the value the interpolator is at
        val value = it.animatedValue as Int
        // I'm going to set the layout's height 1:1 to the tick
        v.layoutParams.height = value
        // force all layouts to see which ones are affected by
        // this layouts height change
        v.requestLayout()
    }

    val set = AnimatorSet()
    set.play(va)
    set.interpolator = AccelerateDecelerateInterpolator()
    set.start();
}

fun getStatus(status: Int): String {

    var code = "START"

    when (status) {
        1 -> return "DRIVE"
        2 -> return "STOP"
        3 -> return "START"
        4 -> return "END"
        5 -> return "EVENT"
    }

    return code
}

fun getStatusImage(status: Int): Int {

    var code = R.drawable.start_flag

    when (status) {
        1 -> return R.drawable.ic_drive
        2 -> return R.drawable.ic_park
        3 -> return R.drawable.start_flag
        4 -> return R.drawable.end_flag
        5 -> return R.drawable.ic_event
    }


    return code
}

fun getColor(color: String): String {

    var code = "#ffd700"

    when (color) {
        "yellow" -> return "#ffd700"
        "red" -> return "#ff0000"
        "blue" -> return "#1034a6"
        "green" -> return "#228b22"
    }


    return code
}

fun getCar(color: String): Int {

    when (color) {
        "yellow" -> return R.drawable.map_default_idle
        "red" -> return R.drawable.map_default_stop
        "blue" -> return R.drawable.map_default_inactive
        "green" -> return R.drawable.map_default_running
    }

    return return R.drawable.map_default_nodata
}

fun getMapTypes(type: String): Int {

    when (type) {
        "MAP_TYPE_NORMAL" -> return GoogleMap.MAP_TYPE_NORMAL
        "MAP_TYPE_SATELLITE" -> return GoogleMap.MAP_TYPE_SATELLITE
        "MAP_TYPE_HYBRID" -> return GoogleMap.MAP_TYPE_HYBRID
        "MAP_TYPE_TERRAIN" -> return GoogleMap.MAP_TYPE_TERRAIN
    }

    return return GoogleMap.MAP_TYPE_NORMAL
}

@SuppressLint("SimpleDateFormat")
fun getCurrentDay(): String {
    val df = SimpleDateFormat("yyyy-MM-dd\nhh:mm aa")
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.AM_PM, Calendar.AM)
    return df.format(calendar.timeInMillis).replace("PG", "AM").replace("PTG", "PM")
}

@SuppressLint("SimpleDateFormat")
fun nextDay(): String {
    val df = SimpleDateFormat("yyyy-MM-dd\nhh:mm aa")
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR, 11)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.AM_PM, Calendar.PM)
    return df.format(calendar.timeInMillis).replace("PG", "AM").replace("PTG", "PM")
}

@SuppressLint("SimpleDateFormat")
fun nextDayAM(): String {
    val df = SimpleDateFormat("yyyy-MM-dd\nhh:mm aa")
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.AM_PM, Calendar.AM)
    return df.format(calendar.timeInMillis).replace("PG", "AM").replace("PTG", "PM")
}


@SuppressLint("SimpleDateFormat")
fun getPreviousDays(keyword: String): String {
    val df = SimpleDateFormat("yyyy-MM-dd\nhh:mm aa")
    val calendar = Calendar.getInstance()

    when (keyword) {
        "today" -> {
            return df.format(calendar.timeInMillis).replace("PG", "AM").replace("PTG", "PM")
        }
        "yesterday" -> {
            calendar.add(Calendar.DATE, -1)
            return df.format(calendar.timeInMillis).replace("PG", "AM").replace("PTG", "PM")
        }
        "week" -> {
            calendar.add(Calendar.DATE, -7)
            return df.format(calendar.timeInMillis).replace("PG", "AM").replace("PTG", "PM")
        }
        "last week" -> {
            calendar.add(Calendar.DATE, -7)
            return df.format(calendar.timeInMillis).replace("PG", "AM").replace("PTG", "PM")
        }
        "month" -> {
            calendar.add(Calendar.DATE, -30)
            return df.format(calendar.timeInMillis).replace("PG", "AM").replace("PTG", "PM")
        }
    }

    return df.format(calendar.timeInMillis).replace("PG", "AM").replace("PTG", "PM")
}

@SuppressLint("SimpleDateFormat")
fun getPreviousTime(keyword: String): String {
    val df = SimpleDateFormat("yyyy-MM-dd")
    val calendar = Calendar.getInstance()

    when (keyword) {
        "next_day" -> {
            calendar.add(Calendar.DATE, +1)
            return df.format(calendar.timeInMillis)
        }
        "today" -> {
            return df.format(calendar.timeInMillis)
        }
        "yesterday" -> {
            calendar.add(Calendar.DATE, -1)
            return df.format(calendar.timeInMillis)
        }
        "two_days" -> {
            calendar.add(Calendar.DATE, -2)
            return df.format(calendar.timeInMillis)
        }
        "three_days" -> {
            calendar.add(Calendar.DATE, -3)
            return df.format(calendar.timeInMillis)
        }
        "week_start" -> {
            while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                calendar.add(Calendar.DATE, -1);
            }
            return df.format(calendar.timeInMillis)
        }
        "week_end" -> {
            val calInstance = Calendar.getInstance()
            while (calInstance.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                calInstance.add(Calendar.DATE, -1);
            }

            calInstance.add(Calendar.DATE, +7)
            return df.format(calInstance.timeInMillis)
        }
        "last_week_start" -> {
            val calInstance = Calendar.getInstance()
            while (calInstance.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                calInstance.add(Calendar.DATE, -1);
            }

            calInstance.add(Calendar.DATE, -7)
            return df.format(calInstance.timeInMillis)
        }
        "last_week_end" -> {
            while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                calendar.add(Calendar.DATE, -1);
            }
            return df.format(calendar.timeInMillis)
        }
        "month_start" -> {
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH))
            return df.format(calendar.timeInMillis)
        }
        "month_end" -> {
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            return df.format(calendar.timeInMillis)
        }
        "last_month" -> {
            calendar.add(Calendar.MONTH, -1);
            calendar.set(Calendar.DATE, 1);
            return df.format(calendar.timeInMillis)
        }
    }

    return df.format(calendar.timeInMillis)
}


fun getReportTypeId(type: String): Int {
    val mType = 1
    when (type) {
        "General Information" -> return 1
        "Travel Sheet" -> return 4
        "Events" -> return 8
        "Travel Sheet Custom" -> return 39
        "OverSpeed" -> return 5
        "Geofence in/out" -> return 7
        "Fuel Level" -> return 10
        "Temperature" -> return 13
        "Engine Hours Daily" -> return 29
        "Ignition ON/OFF" -> return 30
        "Expenses" -> return 46
        "Fuel Filling" -> return 11
        "Fuel Theft" -> return 12
        "Work Hours Daily" -> return 48
    }
    return mType
}

fun getReportPosition(id: Int): Int {
    val map = HashMap<Int, Int>()

    map[1] = 0
    map[4] = 1
    map[39] = 2
    map[8] = 3
    map[10] = 4
    map[13] = 5
    map[29] = 6
    map[30] = 7
    map[46] = 8
    map[11] = 9
    map[12] = 10
    map[48] = 11

    return map[id]!!
}

fun getReportPosition(type: String): Int {
    var pos = 0
    when (type) {
        "pdf" -> pos = 0
        "pdf_land" -> pos = 1
        "xls" -> pos = 2
        "html" -> pos = 3
    }

    return pos
}

fun arePermissionsGranted(context: Context, permissions: Array<String>): Boolean {
    for (permission in permissions) {
        if (ContextCompat.checkSelfPermission(
                context,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        )
            return false

    }
    return true
}

fun isAtLeastVersion(version: Int): Boolean {
    return Build.VERSION.SDK_INT >= version
}

fun getMimeType(extension: String): String {
    when (extension) {
        ".pdf" -> {
            return "application/pdf"
        }
        ".xls" -> {
            return "application/vnd.ms-excel"
        }
        ".html" -> {
            return "text/html"
        }
    }

    return "application/pdf"
}

fun getReportFormat(position: Int): String {
    when (position) {
        0 -> {
            return "pdf"
        }
        1 -> {
            return "pdf_land"
        }
        2 -> {
            return "xls"
        }
        3 -> {
            return "html"
        }
    }

    return "application/pdf"
}

fun getCommandList(): MutableList<TemplateData> {
    return mutableListOf(
        TemplateData(-1, "Custom Command", "")
    )
}

fun isWhite(color: String): Int {

    var rawFontColor: String = color
    if (color.startsWith("#")) {
        rawFontColor = color.substring(1, color.length)
    }

    val rgb: Int = rawFontColor.toInt(16)

    if (ColorUtils.calculateLuminance(rgb) < 0.25) {
        return R.style.TileTitleOne
    }

    return R.style.TileTitleTwo
}

fun getNewTimeFormat(time: String): String {

    Log.e("time : ", time)

    if (time.isEmpty()) {
        return ""
    } else {

        if (time.contains("pm", true)) {
            val newTime = time.replace("PM", "")
            val newTimeArr = newTime.split(":")

            var joiner = ""

            joiner = if (newTimeArr[0] == "12") {
                "12"
            } else {
                val item = parseInt(newTimeArr[0]) + 12
                if (item < 24) {
                    "" + item
                } else {
                    newTimeArr[0]
                }
            }

            return joiner + ":" + newTimeArr[1].replace(" ", "")
        } else {

            var joiner = ""

            val newTime = time.replace("AM", "")
            val newTimeArr = newTime.split(":")

            joiner = if (newTimeArr[0] == "12") {
                "00"
            } else {
                newTimeArr[0]
            }

            return joiner + ":" + newTimeArr[1].replace(" ", "")
        }
    }

    return time.replace(" ", "")
}


@SuppressLint("SimpleDateFormat")
fun differenceInDay(newDate: String): Int {

    return try {
        val dates = SimpleDateFormat("yyyy-MM-dd")
        val finalDate = dates.format(Calendar.getInstance().timeInMillis)

        val date1 = dates.parse(newDate)
        val date2 = dates.parse(finalDate)

        val difference: Long = kotlin.math.abs(date1.time - date2.time)
        val differenceDates = difference / (24 * 60 * 60 * 1000)
        differenceDates.toInt()
    } catch (ex: Exception) {
        Log.e("Error to ", " parse $newDate")
        0
    }

}