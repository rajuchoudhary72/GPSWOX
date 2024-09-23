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
import com.shazcom.gps.app.databinding.ItemDeviceChildBinding
import com.shazcom.gps.app.ui.activities.MapPage
import com.shazcom.gps.app.utils.getCar
import com.shazcom.gps.app.utils.getColor


class DeviceChildAdapter(private val itemList: List<Items>) :
    RecyclerView.Adapter<DeviceChildAdapter.DeviceChildAdapterViewHolder>() {

    class DeviceChildAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val childTitle = ItemDeviceChildBinding.bind(view).headerTxt!!
        val dateTxt =  ItemDeviceChildBinding.bind(view).dateTxt!!
        val colorDot =  ItemDeviceChildBinding.bind(view).dotColor!!
        val speed =  ItemDeviceChildBinding.bind(view).speed!!
        val icon =  ItemDeviceChildBinding.bind(view).icon!!
        val address =  ItemDeviceChildBinding.bind(view).address!!
        val duration =  ItemDeviceChildBinding.bind(view).duration!!
        val rootView = view.rootView!!
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DeviceChildAdapterViewHolder {
        val binding = ItemDeviceChildBinding.inflate(
            LayoutInflater.from(parent.context), parent,
            false
        )
        return DeviceChildAdapterViewHolder(
            binding.root
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