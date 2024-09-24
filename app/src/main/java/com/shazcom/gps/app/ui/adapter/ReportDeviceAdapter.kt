package com.shazcom.gps.app.ui.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.DeviceData
import com.shazcom.gps.app.data.response.Items
import kotlinx.android.synthetic.main.item_device_report.view.*

class ReportDeviceAdapter(
    private val deviceList: List<DeviceData>,
    private val onDeviceChangeListener: (Items) -> Unit
) :
    RecyclerView.Adapter<ReportDeviceAdapter.ReportDeviceAdapterViewHolder>() {


    class ReportDeviceAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.headerTxt
        val childItems = view.childItems
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReportDeviceAdapterViewHolder {
        return ReportDeviceAdapterViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_device_report,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = deviceList.size

    override fun onBindViewHolder(holder: ReportDeviceAdapterViewHolder, position: Int) {

        val item = deviceList[position]
        holder.title.text = "${item?.title} (${item?.items?.size})"
        var childAdapter = ReportDeviceChildAdapter(item?.items!!, onDeviceChangeListener)

        holder.childItems.apply {
            layoutManager = LinearLayoutManager(holder.title.context)
            adapter = childAdapter
        }

        holder.title.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView?.isPressed!!) {
                if (isChecked) {
                    childAdapter.selectAll()
                } else {
                    childAdapter.deselectAll()
                }

                childAdapter.notifyDataSetChanged()
            }
        }

        for (value in item?.items!!) {
            if (value.isChecked != null) {
                if (!value.isChecked!!) {
                    holder.title.isChecked = false
                    return
                } else {
                    holder.title.isChecked = true
                }
            } else {
                holder.title.isChecked = false
            }
        }
    }

    fun selectAll() {
        deviceList?.forEach {
            it.items?.forEach {
                it.isChecked = true
                onDeviceChangeListener(it)
            }
        }
        notifyDataSetChanged()
    }

    fun deSelectAll() {
        deviceList?.forEach {
            it.items?.forEach {
                it.isChecked = false
                onDeviceChangeListener(it)
            }
        }
        notifyDataSetChanged()
    }

    fun setCheckItem(ids : List<Int>) {
        deviceList?.forEach {
            it.items?.forEach { item ->
                if(ids.contains(item.id)) {
                    item.isChecked = true
                    onDeviceChangeListener(item)
                }else{
                    item.isChecked = false
                    onDeviceChangeListener(item)
                }
            }
        }
        notifyDataSetChanged()
    }
}
