package com.shazcom.gps.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.SystemEventData
import kotlinx.android.synthetic.main.item_alert_device.view.*

class CustomEventsAdapter(private val events : List<SystemEventData>) :
    RecyclerView.Adapter<CustomEventsAdapter.CustomEventsViewHolder>() {

    class CustomEventsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkedItem = view.checkedItem!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomEventsViewHolder {
        return CustomEventsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_alert_device,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = events.size

    override fun onBindViewHolder(holder: CustomEventsViewHolder, position: Int) {
        holder.checkedItem.text = events[position].toString().trim()
        holder.checkedItem.isChecked = events[position].isChecked

        holder.checkedItem.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                holder.checkedItem.isChecked = isChecked
                events[position].isChecked = isChecked
            }
        }
    }

    fun checkAll(b: Boolean) {
        for (e in events) {
            e.isChecked = b
        }

        notifyDataSetChanged()
    }

    fun getCheckedItem(): List<Int> {

        val list: ArrayList<Int> = ArrayList()
        for (e in events) {
            if (e.isChecked) {
                list.add(e.id)
            }
        }

        return list
    }

    fun setCheckItems(ids : List<Int>) {
        for (e in events) {
            if (ids.contains(e.id)) {
                e.isChecked = true
            }
        }

        notifyDataSetChanged()
    }
}