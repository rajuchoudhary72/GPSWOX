package com.shazcom.gps.app.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.EventData
import com.shazcom.gps.app.ui.activities.EventDetail
import kotlinx.android.synthetic.main.item_event.view.*

class EventAdapter(events: List<EventData>) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private var dataItems: MutableList<EventData>? = null

    init {
        dataItems = events as MutableList<EventData>
    }

    fun addNewItems(data: List<EventData>) {
        dataItems?.addAll(data)
        notifyDataSetChanged()
    }

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventName = view.eventName!!
        val eventTime = view.time!!
        val speed = view.speed!!
        val rootView = view.rootView!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        return EventViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_event,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = dataItems?.size!!

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val item = dataItems?.get(position)
        holder.eventName.text = item?.message
        holder.eventTime.text = item?.time

        if (item?.name.equals("Overspeed", true)) {
            holder.speed.text = "with ${item?.speed} kph speed"
            holder.speed.visibility = View.VISIBLE
        } else {
            holder.speed.visibility = View.INVISIBLE
        }

        holder.rootView.setOnClickListener {
            Intent(holder.rootView.context, EventDetail::class.java).apply {
                putExtra("eventItem", item)
                holder.rootView.context.startActivity(this)
            }
        }
    }
}