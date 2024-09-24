package com.shazcom.gps.app.ui.activities

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.shazcom.gps.app.GPSWoxApp
import com.shazcom.gps.app.R
import com.shazcom.gps.app.back.DeviceService
import com.shazcom.gps.app.back.DeviceServiceConstants
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.ToolsRepository
import com.shazcom.gps.app.data.response.DeviceData
import com.shazcom.gps.app.data.response.Groups
import com.shazcom.gps.app.data.response.Items
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.BaseActivity
import com.shazcom.gps.app.ui.fragments.Reports
import com.shazcom.gps.app.ui.viewmodal.ToolsViewModel
import kotlinx.android.synthetic.main.activity_dashboard.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


@Suppress("DEPRECATION")
class Dashboard : BaseActivity(), KodeinAware, NavController.OnDestinationChangedListener {

    override val kodein by kodein()
    private val localDB: LocalDB by instance<LocalDB>()
    private var doubleBackToExitPressedOnce = false

    private var routeItemList: ArrayList<Items>? = null
    private var deviceDataList: List<DeviceData>? = null
    private var groupsData: List<Groups>? = null
    private var navController: NavController? = null

    private val repository: ToolsRepository by instance<ToolsRepository>()
    private var toolsViewModel: ToolsViewModel? = null


    var mServiceIntent: Intent? = null
    private var deviceService: DeviceService? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        deviceService = DeviceService()
        mServiceIntent = Intent(this, DeviceService::class.java)
        if (!isMyServiceRunning(DeviceService::class.java)) {
            mServiceIntent?.action = DeviceServiceConstants.ACTION_START_SERVICE
            startService(mServiceIntent)
        }

        setContentView(R.layout.activity_dashboard)

        toolsViewModel = ViewModelProvider(this).get(ToolsViewModel::class.java)
        toolsViewModel?.toolsRepository = repository

        loadGeofence()
        loadPOIMarkers()

        navController = Navigation.findNavController(this, R.id.fragment)
        NavigationUI.setupWithNavController(navView, navController!!)

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        nav_icon.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.openDrawer(
                GravityCompat.START
            )
            else drawerLayout.closeDrawer(GravityCompat.END)
        }

        navController?.addOnDestinationChangedListener(this)
        navView.setCheckedItem(0)

        navView.menu.findItem(R.id.logout).setOnMenuItemClickListener {
            doLogout()
            return@setOnMenuItemClickListener true
        }


        navView.menu.findItem(R.id.customerCall).setOnMenuItemClickListener {
            openDialer()
            drawerLayout.closeDrawers()
            return@setOnMenuItemClickListener true
        }

        navView.menu.findItem(R.id.customerEmail).setOnMenuItemClickListener {
            openEmail()
            drawerLayout.closeDrawers()
            return@setOnMenuItemClickListener true
        }

/*        navView.menu.findItem(R.id.gprsCommand).setOnMenuItemClickListener {
            Intent(this, GprsCommand::class.java).apply {
                startActivity(this)
            }
            return@setOnMenuItemClickListener true
        }*/


        back_icon.setOnClickListener {
            navController?.navigateUp()
        }

        FirebaseApp.initializeApp(this@Dashboard)
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("TrackProGps", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result
                localDB.saveFcmToken(token!!)

                // Log and toast
                val msg = getString(R.string.msg_token_fmt, token)
                Log.d("TrackProGps", msg)
            })

        saveFcmToken()
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(
            Navigation.findNavController(this, R.id.fragment), drawerLayout
        )
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {

        if (destination.id == R.id.status) {
            search_icon.visibility = View.VISIBLE
        } else {
            search_icon.visibility = View.INVISIBLE
        }

        navTitle.text = destination.label
    }

    fun saveDevices(list: ArrayList<Items>) {
        routeItemList = list
    }

    fun getItemList(): ArrayList<Items> {
        return routeItemList!!
    }

    fun saveAllDevices(deviceList: List<DeviceData>) {
        deviceDataList = deviceList
    }

    fun getDeviceList(): List<DeviceData> {
        return deviceDataList!!
    }

    fun saveGroups(groups: List<Groups>) {
        groupsData = groups
    }

    fun getGroups(): List<Groups> {
        return groupsData!!
    }

    override fun onBackPressed() {
        handleBackButton(this@Dashboard)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {

            val fragment = supportFragmentManager.fragments[0]
            val childFragment = fragment.childFragmentManager.fragments[0]
            if (childFragment != null && childFragment is Reports) {
                childFragment.hideBottomSheetFromOutSide(event)
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    fun loadGeofence() {
        toolsViewModel?.loadGeoFence("en", localDB.getToken()!!)
            ?.observe(this, Observer { resources ->
                when (resources.status) {
                    Status.SUCCESS -> {
                        (application as GPSWoxApp).saveGeofence(resources?.data?.items?.geofences)
                    }
                    else ->{}
                }
            })
    }


    fun loadPOIMarkers() {
        toolsViewModel?.loadPoiMarkers("en", localDB.getToken()!!)
            ?.observe(this, Observer { resources ->
                when (resources.status) {
                    Status.SUCCESS -> {
                        (application as GPSWoxApp).savePoiMarkers(resources?.data?.items?.mapIcons)
                    }
                    else ->{}
                }
            })
    }

    private fun saveFcmToken() {

        localDB?.getFcmToken()?.let {
            toolsViewModel?.saveToken(localDB.getToken()!!, it)
                ?.observe(this, Observer { resources ->
                    when (resources.status) {
                        Status.SUCCESS -> {
                            Log.e("saveFcmToken", "${localDB.getFcmToken()}")
                        }
                        Status.ERROR -> {
                            Log.e("saveFcmToken", "${resources.message}")
                            resources?.code?.let {
                                if (it == 401) {
                                    navController?.navigate(R.id.account)
                                }
                            }
                        }
                        else ->{}
                    }
                })
        }
    }


    private fun handleBackButton(context: Context) {

        context?.let {
            //if (doubleBackToExitPressedOnce) {
                if (navView.checkedItem?.itemId == R.id.status) {
                    finish()
                } else {
                    navController?.navigate(R.id.status)
                }
                return
            //}

            //doubleBackToExitPressedOnce = true
            //Toast.makeText(context, getString(R.string.press_back_again), Toast.LENGTH_SHORT).show()
            //Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 3000)
        }
    }


    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                Log.i("Service status", "Running")
                return true
            }
        }
        Log.i("Service status", "Not running")
        return false
    }


    override fun onResume() {
        super.onResume()
        val app = application as GPSWoxApp
        app.appKilled = true
    }

    override fun onDestroy() {
        super.onDestroy()
        val app = application as GPSWoxApp
        app.appKilled = false

        if (isMyServiceRunning(DeviceService::class.java)) {
            mServiceIntent?.action = DeviceServiceConstants.ACTION_STOP_SERVICE
            startService(mServiceIntent)
        }
    }

}