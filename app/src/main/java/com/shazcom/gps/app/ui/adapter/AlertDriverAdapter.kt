package com.shazcom.gps.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.AlertOption
import kotlinx.android.synthetic.main.item_alert_device.view.*

class AlertDriverAdapter(private val driverList : List<AlertOption>) :
    RecyclerView.Adapter<AlertDriverAdapter.AlertDriverViewHolder>() {

    class AlertDriverViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkedItem = view.checkedItem!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertDriverViewHolder {
        return AlertDriverViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_alert_device,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = driverList.size

    override fun onBindViewHolder(holder: AlertDriverViewHolder, position: Int) {
        holder.checkedItem.text = driverList[position].toString()
        holder.checkedItem.isChecked = driverList[position].isChecked

        holder.checkedItem.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                holder.checkedItem.isChecked = isChecked
                driverList[position].isChecked = isChecked
            }
        }
    }

    fun checkAll(b: Boolean) {
        for (d in driverList) {
            d.isChecked = b
        }

        notifyDataSetChanged()
    }

    fun getCheckedItem(): List<Int> {

        val list: ArrayList<Int> = ArrayList()
        for (d in driverList) {
            if (d.isChecked) {
                list.add(d.id)
            }
        }

        return list
    }

    fun setCheckItems(ids : List<Int>) {
        for (d in driverList) {
            if (ids.contains(d.id)) {
               d.isChecked = true
            }
        }

        notifyDataSetChanged()
    }
}