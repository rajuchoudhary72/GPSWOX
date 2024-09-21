package com.shazcom.gps.app.ui.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.Items
import com.shazcom.gps.app.ui.activities.MapPage
import com.shazcom.gps.app.utils.getCar
import com.shazcom.gps.app.utils.getColor
import kotlinx.android.synthetic.main.item_device_child.view.*

class DeviceChildAdapter(private val itemList: List<Items>) :
    RecyclerView.Adapter<DeviceChildAdapter.DeviceChildAdapterViewHolder>() {

    class DeviceChildAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val childTitle = view.headerTxt!!
        val dateTxt = view.dateTxt!!
        val colorDot = view.dotColor!!
        val speed = view.speed!!
        val icon = view.icon!!
        val address = view.address!!
        val duration = view.duration!!
        val rootView = view.rootView!!
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DeviceChildAdapterViewHolder {
        return DeviceChildAdapterViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_device_child,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = itemList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DeviceChildAdapterViewHolder, position: Int) {
        holder.childTitle.text = itemList[position].name
        holder.dateTxt.text = itemList[position].time
        holder.speed.text = "${itemList[position].speed} ${itemList[position].distance_unit_hour}"

        holder.duration.text = "Stop Duration : ${itemList[position].stop_duration}"

        val color: Int = Color.parseColor(itemList[position].icon_color?.let { getColor(it) })
        holder.colorDot.setColorFilter(color)

        holder.address.text = "${itemList[position].address}"
        itemList[position].icon_color?.let { getCar(it) }?.let { holder.icon.setImageResource(it) }

        holder.rootView.setOnClickListener {
            Intent(holder.childTitle.context, MapPage::class.java).apply {
                putExtra("deviceItem", itemList[position])
                holder.childTitle.context.startActivity(this)
            }
        }
    }

}