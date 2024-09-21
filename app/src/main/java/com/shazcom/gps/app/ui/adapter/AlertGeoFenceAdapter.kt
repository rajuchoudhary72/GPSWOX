package com.shazcom.gps.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.AlertOption
import kotlinx.android.synthetic.main.item_alert_device.view.*

class AlertGeoFenceAdapter(private val geofences: List<AlertOption>) :
    RecyclerView.Adapter<AlertGeoFenceAdapter.AlertGeoFenceViewHolder>() {

    class AlertGeoFenceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkedItem = view.checkedItem!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertGeoFenceViewHolder {
        return AlertGeoFenceViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_alert_device,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = geofences.size

    override fun onBindViewHolder(holder: AlertGeoFenceViewHolder, position: Int) {
        holder.checkedItem.text = geofences[position].toString()
        holder.checkedItem.isChecked = geofences[position].isChecked

        holder.checkedItem.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                holder.checkedItem.isChecked = isChecked
                geofences[position].isChecked = isChecked
            }
        }
    }

    fun checkAll(b: Boolean) {
        for (g in geofences) {
            g.isChecked = b
        }

        notifyDataSetChanged()
    }

    fun getCheckedItem(): List<Int> {

        val list: ArrayList<Int> = ArrayList()
        for (g in geofences) {
            if (g.isChecked) {
                list.add(g.id)
            }
        }

        return list
    }

    fun setCheckItems(ids : List<Int>) {
        for (g in geofences) {
            if (ids.contains(g.id)) {
                g.isChecked = true
            }
        }

        notifyDataSetChanged()
    }
}