package com.shazcom.gps.app.network.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.ui.activities.LoginActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.shazcom.gps.app.ui.activities.EventPage
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class FirebaseMessage : FirebaseMessagingService(), KodeinAware {

    override val kodein by kodein()
    private val localDB: LocalDB by instance<LocalDB>()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {


        localDB?.getToken()?.let {
            Log.d(TAG, "From: ${remoteMessage.from}")

            remoteMessage.data.isNotEmpty().let {
                Log.d(TAG, "Message data payload: " + remoteMessage.data)
                //sendNotification(remoteMessage.data.toString())
            }

            if (remoteMessage.data.isEmpty()) {
                remoteMessage.notification?.let {
                    Log.d(TAG, "Message Notification: title ${it.title}")
                    Log.d(TAG, "Message Notification Body: ${it.body}")
                    sendNotification(it.title.toString(), it.body.toString())
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = getIntentData(title)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    private fun getIntentData(title: String): Intent {

        var intent = Intent(this, LoginActivity::class.java)
        localDB.getDeviceList()?.let {
            for (notificationItem in it) {
                if (notificationItem.name == title.split(" ")[0]) {
                    intent = Intent(this, EventPage::class.java).apply {
                        putExtra("deviceId", notificationItem?.id!!)
                        putExtra("deviceName", notificationItem?.name!!)
                        return@let
                    }
                }
            }
        }


        return intent
    }

    companion object {
        private const val TAG = "Shazcom Messaging"
    }
}