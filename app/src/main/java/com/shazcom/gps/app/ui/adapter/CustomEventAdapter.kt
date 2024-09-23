package com.shazcom.gps.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.CustomEventData
import com.shazcom.gps.app.databinding.ItemAlertDeviceBinding
import com.shazcom.gps.app.databinding.ItemGroupBinding


class CustomEventAdapter(private val events: List<CustomEventData>) :
    RecyclerView.Adapter<CustomEventAdapter.DriverViewHolder>() {

    class DriverViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = ItemGroupBinding.bind(view).groupName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DriverViewHolder {

        val item =
            ItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return DriverViewHolder(
            item.rootView
        )
    }

    override fun getItemCount(): Int = events.size

    override fun onBindViewHolder(holder: DriverViewHolder, position: Int) {
        holder.name.text = events[position].protocol
    }
}