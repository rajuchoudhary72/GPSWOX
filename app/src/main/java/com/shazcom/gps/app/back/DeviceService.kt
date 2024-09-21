package com.shazcom.gps.app.back

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import com.shazcom.gps.app.GPSWoxApp
import com.shazcom.gps.app.R
import com.shazcom.gps.app.back.RestartService
import com.shazcom.gps.app.back.DeviceServiceConstants.ACTION_CONNECT_APP
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.CommonViewRepository
import com.shazcom.gps.app.data.response.LatestDeviceData
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.activities.Dashboard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.util.*


object DeviceServiceConstants {
    const val ACTION_START_SERVICE = "START"
    const val ACTION_STOP_SERVICE = "STOP"
    const val ACTION_OPEN_APP = "OPEN"

    const val ACTION_CONNECT_APP = 10001
}


class DeviceService : LifecycleService(), KodeinAware {

    var replyMessenger: Messenger? = null
    private val MAX_INTERVAL = 4000L
    var counter = 0
    override val kodein by kodein()
    private val commonViewRepository: CommonViewRepository by instance()
    private val localDB: LocalDB by instance()
    private var latestDeviceData: LatestDeviceData? = null

    val mMessenger = Messenger(IncomingHandler())

    inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                ACTION_CONNECT_APP -> {
                    replyMessenger = msg.replyTo

                    latestDeviceData?.let {
                        val message = Message()
                        message.obj = latestDeviceData
                        Log.i("DeviceService", "Sending Data" + counter++)
                        replyMessenger?.send(message)
                    }
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return mMessenger.binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        action?.let { action ->
            when (action) {
                DeviceServiceConstants.ACTION_START_SERVICE -> {
                    startTimer()
                    startDeviceForegroundService()
                }
                DeviceServiceConstants.ACTION_STOP_SERVICE -> {
                    stopForegroundService()
                }

                DeviceServiceConstants.ACTION_OPEN_APP -> {
                    val launchIntent =
                        packageManager.getLaunchIntentForPackage("com.shazcom.gps.app")
                    startActivity(launchIntent)
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }




    private fun startDeviceForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("ShazcomService", "GPS Background Service");
        } else {

            // Create notification default intent.
            val intent = Intent()
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

            val builder = NotificationCompat.Builder(this)

            // Make notification show big text.
            val bigTextStyle = NotificationCompat.InboxStyle()
            bigTextStyle.setBigContentTitle("GPS is running")
            // Set big text style.
            builder.setStyle(bigTextStyle)

            builder.setWhen(System.currentTimeMillis())
            builder.setSmallIcon(R.mipmap.ic_launcher)
            val largeIconBitmap =
                BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_background)
            builder.setLargeIcon(largeIconBitmap)
            builder.priority = Notification.PRIORITY_MAX
            builder.setFullScreenIntent(pendingIntent, true)


            // Add Stop button intent in notification.
//            val stopIntent = Intent(this, DeviceService::class.java)
//            stopIntent.action = DeviceServiceConstants.ACTION_STOP_SERVICE
//            val pendingPrevIntent = PendingIntent.getService(this, 0, stopIntent, 0)
//            val stopAction =
//                NotificationCompat.Action(R.drawable.ic_close, "STOP", pendingPrevIntent)
//            builder.addAction(stopAction)

            // Build the notification.
            val notification = builder.build()

            // Start foreground service.
            startForeground(1, notification)
        }
    }


    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String) {

        val resultIntent = Intent(this, Dashboard::class.java)


        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntentWithParentStack(resultIntent)
        val resultPendingIntent =
            if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.S) {
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_MUTABLE)
            }else {
               stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }

        val chan = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)

        // Add Pause button intent in notification.
//        val stopIntent = Intent(this, DeviceService::class.java)
//        stopIntent.action = DeviceServiceConstants.ACTION_STOP_SERVICE
//        val pendingPrevIntent = PendingIntent.getService(this, 0, stopIntent, 0)
//        val stopAction = NotificationCompat.Action(R.drawable.ic_close, "STOP", pendingPrevIntent)


        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.route)
            .setContentTitle("App is running in background")
            .setPriority(NotificationManager.IMPORTANCE_LOW)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentIntent(resultPendingIntent)
            //.addAction(stopAction)
            .build()
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(1, notificationBuilder.build())
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimerTask()

        val broadcastIntent = Intent()
        broadcastIntent.action = "RestartService"
        broadcastIntent.setClass(this, RestartService::class.java)
        this.sendBroadcast(broadcastIntent)
    }


    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    private fun startTimer() {
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {

                val app = application as GPSWoxApp
                Log.i("DeviceService is open", "=========  " + app.appIsRunning)
                Log.i("DeviceService Count", "=========  " + counter++)

                CoroutineScope(Dispatchers.Main).launch {
                    commonViewRepository.getDeviceInfoLatest("en", "${localDB.getToken()}")
                        .observe(this@DeviceService, androidx.lifecycle.Observer { resources ->
                            when (resources.status) {
                                Status.SUCCESS -> {
                                    resources.data?.let { result ->
                                        latestDeviceData = result
                                        val app = application as GPSWoxApp
                                        Log.i("DeviceService Count", "=========  fetch server " + app.appIsRunning)
                                        if (app.appIsRunning) {
                                            val message = Message()
                                            message.obj = result
                                            Log.i("DeviceService", "Sending Data" + counter++)
                                            replyMessenger?.send(message)
                                        }
                                    }
                                }
                            }
                        })
                }
            }
        }

        timer?.schedule(timerTask, MAX_INTERVAL, MAX_INTERVAL)
    }

    private fun stopTimerTask() {
        if (timer != null) {
            timer?.cancel()
            timer = null
        }
    }


    private fun stopForegroundService() {
        val app = application as GPSWoxApp
        if (!app.appKilled) {
            stopForeground(true)
            stopSelf()
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.i("DeviceService ", "=========  APP KILLED")
        stopForegroundService()
    }
}