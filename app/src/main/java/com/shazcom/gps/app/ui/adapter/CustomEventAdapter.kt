package com.shazcom.gps.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.CustomEventData
import kotlinx.android.synthetic.main.item_group.view.*

class CustomEventAdapter(private val events: List<CustomEventData>) :
    RecyclerView.Adapter<CustomEventAdapter.DriverViewHolder>() {

    class DriverViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.groupName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DriverViewHolder {
        return DriverViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_group,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = events.size

    override fun onBindViewHolder(holder: DriverViewHolder, position: Int) {
        holder.name.text = events[position].protocol
    }
}