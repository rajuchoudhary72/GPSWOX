package com.shazcom.gps.app.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.AlertDevices
import kotlinx.android.synthetic.main.item_alert_device.view.*

class AlertDeviceAdapter(private val devices: List<AlertDevices>) :
    RecyclerView.Adapter<AlertDeviceAdapter.AlertDeviceViewHolder>() {

    class AlertDeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkedItem = view.checkedItem!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertDeviceViewHolder {
        return AlertDeviceViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_alert_device,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = devices.size

    override fun onBindViewHolder(holder: AlertDeviceViewHolder, position: Int) {
        holder.checkedItem.text = devices[position].toString()
        holder.checkedItem.isChecked = devices[position].isChecked

        holder.checkedItem.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                holder.checkedItem.isChecked = isChecked
                devices[position].isChecked = isChecked
            }
        }
    }

    fun checkAll(b: Boolean) {
        for (d in devices) {
            d.isChecked = b
        }

        notifyDataSetChanged()
    }

    fun getCheckedItem(): List<Int> {

        val list: ArrayList<Int> = ArrayList()
        for (d in devices) {
            if (d.isChecked) {
                list.add(d.id)
            }
        }

        return list
    }

    fun setCheckItems(ids : List<Int>) {
        for (d in devices) {
            Log.e("Ids ","${d.id}")
            if(ids.contains(d.id)) {
                d.isChecked = true
            }
        }

        notifyDataSetChanged()
    }
}