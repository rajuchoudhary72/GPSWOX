package com.shazcom.gps.app.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.utils.Constants.KEYWORD
import com.shazcom.gps.app.utils.Constants.LOCATION_DATA_EXTRA
import com.shazcom.gps.app.utils.Constants.RECEIVER
import com.shazcom.gps.app.utils.Constants.RESULT_DATA_KEY
import com.shazcom.gps.app.utils.Constants.SUCCESS_RESULT
import com.shazcom.gps.app.utils.FetchAddressIntentService
import com.shazcom.gps.app.utils.LocaleHelper
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.util.*


open class BaseActivity : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()
    private val localDB: LocalDB by instance<LocalDB>()
    val TAG = BaseActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initConfiguration()
        //adjustDisplayDensity()
        //adjustFontScale()
        askNotificationPermission()
    }

    private fun adjustDisplayDensity() {
        val displayMetrics = resources.displayMetrics
        val snap = 20
        val exactDpi = (displayMetrics.xdpi + displayMetrics.ydpi) / 0.4
        val dpi = displayMetrics.densityDpi.toFloat()
        if (dpi - exactDpi > snap) {
            val targetDpi = (Math.ceil((exactDpi / snap)) * snap).toInt()
            val log = "Changing DPI from $dpi to $targetDpi"
            Log.w(TAG, log)
            val config = resources.configuration
            displayMetrics.densityDpi = targetDpi
            config.densityDpi = targetDpi
            displayMetrics.setTo(displayMetrics)
            config.setTo(config)
            resources.updateConfiguration(config, displayMetrics)
        }
    }

    private fun adjustFontScale() {
        val configuration = resources.configuration
        val fontScale = configuration.fontScale
        val targetFontScale = 1.0f
        if (fontScale > targetFontScale) {
            val log =
                String.format("Changing font scale from %.2f to %.2f", fontScale, targetFontScale)
            Log.w(TAG, log)
            configuration.fontScale = targetFontScale
            val metrics = resources.displayMetrics
            val wm = getSystemService(WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay.getMetrics(metrics)
            metrics.scaledDensity = configuration.fontScale * metrics.density
            configuration.setLocale(Locale(LocaleHelper.getLanguage(this@BaseActivity)))
            baseContext.resources.updateConfiguration(configuration, metrics)
        }
    }


    private fun initConfiguration() {

        val resources: Resources = resources
        val configuration = resources.configuration
        configuration.fontScale = 0.9f

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        metrics.scaledDensity = configuration.fontScale * metrics.density
        configuration.densityDpi = resources.displayMetrics.xdpi.toInt()

        configuration.setLocale(Locale(LocaleHelper.getLanguage(this@BaseActivity)))
        applicationContext.createConfigurationContext(configuration)

        Log.e("Current Language", "${LocaleHelper.getLanguage(this@BaseActivity)}")
        onResume()

    }

    fun showSnackbar(
        message: String,
        actionStrId: Int = 0,
        listener: View.OnClickListener? = null
    ) {
        val snackbar = Snackbar.make(
            findViewById(android.R.id.content), message,
            LENGTH_SHORT
        )
        if (actionStrId != 0 && listener != null) {
            snackbar.setAction(getString(actionStrId), listener)
        }

        snackbar.show()
    }

    fun doLogout() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.want_logout))
        builder.setPositiveButton(
            getString(R.string.logout)
        ) { dialog, which ->
            localDB?.removeAll(this@BaseActivity)
            dialog.dismiss()
        }

        builder.setNegativeButton(
            "Cancel"
        ) { dialog, which -> dialog?.dismiss() }

        builder.create().show()
    }

    private inner class AddressResultReceiver internal constructor(
        handler: Handler
    ) : ResultReceiver(handler) {

        /**
         * Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {

            var addressOutput = resultData.getString(RESULT_DATA_KEY)
            var keyword = resultData.getString(KEYWORD)

            if (resultCode == SUCCESS_RESULT) {
                when (keyword) {
                    "PARK_START" -> {
                        popUpAddressWithKeyWord(addressOutput!!, keyword)
                    }

                    "PARK_STOP" -> {
                        popUpAddressWithKeyWord(addressOutput!!, keyword)
                    }

                    "DRIVE_START" -> {
                        popUpAddressWithKeyWord(addressOutput!!, keyword)
                    }

                    "DRIVE_STOP" -> {
                        popUpAddressWithKeyWord(addressOutput!!, keyword)
                    }

                    else -> {
                        popUpAddress(addressOutput!!)
                    }
                }
            } else {
                popUpAddress(getString(R.string.unable_get_location))
            }
        }
    }

    open fun popUpAddress(addressOutput: String) {
    }

    open fun popUpAddressWithKeyWord(addressOutput: String, keyword: String) {
    }

    fun startIntentService(location: Location) {
        val resultReceiver = AddressResultReceiver(Handler())
        val intent = Intent(this, FetchAddressIntentService::class.java).apply {
            putExtra(RECEIVER, resultReceiver)
            putExtra(LOCATION_DATA_EXTRA, location)
        }
        startService(intent)
    }

    fun startIntentServiceWithKeyword(location: Location, keyword: String) {
        val resultReceiver = AddressResultReceiver(Handler())
        val intent = Intent(this, FetchAddressIntentService::class.java).apply {
            putExtra(RECEIVER, resultReceiver)
            putExtra(KEYWORD, keyword)
            putExtra(LOCATION_DATA_EXTRA, location)
        }
        startService(intent)
    }


    override fun onResume() {
        super.onResume()
        LocaleHelper.onAttach(this@BaseActivity)
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.e("New Locale", "" + newConfig.locale)
    }

    fun openDialer() {
        val u: Uri = Uri.parse("tel:+6799990433")
        val i = Intent(Intent.ACTION_DIAL, u)
        try {
            startActivity(i)
        } catch (s: SecurityException) {

        }
    }

    fun openEmail() {
        val TO = "accounts@shazcom.net"//listOf<String>("eva@tatacommunications.com")
        val emailIntent = Intent(Intent.ACTION_SENDTO)

        emailIntent.data = Uri.parse("mailto:$TO")
        //emailIntent.type = "plain/text"
        //emailIntent.putExtra(Intent.EXTRA_EMAIL, TO.toTypedArray())
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");

        try {
            //startActivity(Intent.createChooser(emailIntent, "Send mail..."))
            startActivity(emailIntent)
        } catch (ex: ActivityNotFoundException) {

        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED -> {
                    // Permission is already granted
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    // Show rationale to explain why the permission is needed
                    showPermissionRationale()
                }

                else -> {
                    // Request permission for the first time
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun handlePermissionDenial() {
        // Show Snackbar to take the user to settings if permission was denied
        Snackbar.make(
            findViewById(android.R.id.content),
            getString(R.string.txt_error_post_notification, getString(R.string.app_name)),
            Snackbar.LENGTH_INDEFINITE
        ).setAction(getString(R.string.goto_settings)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val settingsIntent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                startActivity(settingsIntent)
            }
        }.show()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showPermissionRationale() {
        // Show an explanation to the user why the permission is needed and ask again
        Snackbar.make(
            findViewById(android.R.id.content),
            "We need notification permission to send you updates. Please allow it.",
            Snackbar.LENGTH_INDEFINITE
        ).setAction("OK") {
            // Request permission again after showing the rationale
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }.show()
    }

   private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
           // Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT).show()
        } else {
            // Handle permission denial and show rationale or direct user to settings
            handlePermissionDenial()
        }
    }
}