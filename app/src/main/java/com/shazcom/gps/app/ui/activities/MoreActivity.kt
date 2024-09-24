package com.shazcom.gps.app.ui.activities

import android.content.Intent
import android.os.Bundle
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.Items
import com.shazcom.gps.app.ui.BaseActivity
import kotlinx.android.synthetic.main.activity_more.alertNotification
import kotlinx.android.synthetic.main.activity_more.command
import kotlinx.android.synthetic.main.activity_more.service
import kotlinx.android.synthetic.main.activity_more.setGeoFence
import kotlinx.android.synthetic.main.activity_more.toolBar

class MoreActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more)

        toolBar.setNavigationOnClickListener { onBackPressed() }

        command.setOnClickListener {
            Intent(this, GprsCommand::class.java).apply {
                putExtra("item", this@MoreActivity.intent.getParcelableExtra<Items>("item"))
                startActivity(this)
            }
        }

        alertNotification.setOnClickListener {
            Intent(this, AlertActivity::class.java).apply {
                putExtra("item", this@MoreActivity.intent.getParcelableExtra<Items>("item"))
                startActivity(this)
            }
        }

        setGeoFence.setOnClickListener {
            Intent(this, GeoFenceActivity::class.java).apply {
                putExtra("item", this@MoreActivity.intent.getParcelableExtra<Items>("item"))
                startActivity(this)
            }
        }

        service.setOnClickListener {
            val deviceItem = intent?.extras?.get("item") as Items?
            val odometerValue = "0.0"
            val engineLoadValue = "0.0"
            Intent(this, ServicePage::class.java).apply {
                putExtra("deviceId", deviceItem?.id!!)
                putExtra("deviceName", deviceItem.name!!)
                putExtra("odometer", odometerValue)
                putExtra("engineLoad", engineLoadValue)
                startActivity(this)
            }
        }
    }
}