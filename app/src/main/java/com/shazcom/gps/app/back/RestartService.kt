package com.shazcom.gps.app.back

import android.bluetooth.BluetoothClass
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build


class RestartService : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.startForegroundService(
                Intent(
                    context,
                    BluetoothClass.Device::class.java
                )
            )
        } else {
            context?.startService(intent)
        }
    }
}