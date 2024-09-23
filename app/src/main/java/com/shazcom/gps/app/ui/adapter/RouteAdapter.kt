package com.shazcom.gps.app.ui.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shazcom.gps.app.GPSWoxApp
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.HistoryData
import com.shazcom.gps.app.data.response.ItemsInner
import com.shazcom.gps.app.databinding.ItemRouteBinding
import com.shazcom.gps.app.ui.activities.HistoryDetailPage
import com.shazcom.gps.app.utils.getStatus
import com.shazcom.gps.app.utils.getStatusImage


class RouteAdapter(
    private val routeList: List<ItemsInner>,
    private val deviceName: String,
    private val historyData : HistoryData,
    private val gpsWoxApp : GPSWoxApp
) :

    RecyclerView.Adapter<RouteAdapter.RouteAdapterViewHolder>() {

    class RouteAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val routeImage =ItemRouteBinding.bind(view).routeImage!!
        val timeTxt = ItemRouteBinding.bind(view).timeTxt!!
        val eventName = ItemRouteBinding.bind(view).eventName!!
        val eventTxt = ItemRouteBinding.bind(view).eventTxt!!
        val rootView = ItemRouteBinding.bind(view).rootView!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteAdapterViewHolder {
        return RouteAdapterViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_route,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = routeList?.size!!

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RouteAdapterViewHolder, position: Int) {

        val item = routeList[position]
        holder.routeImage.setImageResource(getStatusImage(item.status!!))
        holder.timeTxt.text = "${item.items[0].time}"

        if (item.status == 5) {
            holder.eventName.text = "${item.message}"
        } else if (item.status == 2 || item.status == 1) {
            holder.eventName.text = "Duration : ${item.time}"
        } else {
            holder.eventName.text = ""
        }

        holder.eventTxt.text = getStatus(item.status)
        holder.rootView.setOnClickListener {
            gpsWoxApp.setInnerItem(item)
            Intent(holder.rootView.context , HistoryDetailPage::class.java).apply {
                this.putExtra("distance_route", historyData.distance_sum)
                this.putExtra("move_duration", historyData.move_duration)
                this.putExtra("stop_duration", historyData.stop_duration)
                this.putExtra("fuel_cons", historyData.fuel_consumption)
                this.putExtra("top_speed", historyData.top_speed)
                this.putExtra("deviceName", deviceName)
                holder.rootView.context .startActivity(this)
            }
        }
    }
}