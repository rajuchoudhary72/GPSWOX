package com.shazcom.gps.app.ui.activities

import android.content.Intent
import android.os.Bundle

import com.shazcom.gps.app.data.response.Items
import com.shazcom.gps.app.databinding.ActivityMoreBinding
import com.shazcom.gps.app.ui.BaseActivity


class MoreActivity : BaseActivity() {

    private lateinit var binding: ActivityMoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolBar.setNavigationOnClickListener { onBackPressed() }

        binding.command.setOnClickListener {
            Intent(this, GprsCommand::class.java).apply {
                putExtra("item", this@MoreActivity.intent.getParcelableExtra<Items>("item"))
                startActivity(this)
            }
        }

        binding.alertNotification.setOnClickListener {
            Intent(this, AlertActivity::class.java).apply {
                putExtra("item", this@MoreActivity.intent.getParcelableExtra<Items>("item"))
                startActivity(this)
            }
        }

        binding.setGeoFence.setOnClickListener {
            Intent(this, GeoFenceActivity::class.java).apply {
                putExtra("item", this@MoreActivity.intent.getParcelableExtra<Items>("item"))
                startActivity(this)
            }
        }

        binding.service.setOnClickListener {
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