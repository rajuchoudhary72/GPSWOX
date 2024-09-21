package com.shazcom.gps.app.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.Items
import kotlinx.android.synthetic.main.item_device_child_report.view.*

class ReportDeviceChildAdapter(
    private val itemList: List<Items>,
    private val onDeviceChangeListener: (Items) -> Unit
) :
    RecyclerView.Adapter<ReportDeviceChildAdapter.ReportDeviceChildAdapterViewHolder>() {

    class ReportDeviceChildAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val childTitle = view.headerTxt!!
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReportDeviceChildAdapterViewHolder {
        return ReportDeviceChildAdapterViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_device_child_report,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = itemList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ReportDeviceChildAdapterViewHolder, position: Int) {
        val item = itemList[position]
        holder.childTitle.text = item.name
        holder.childTitle.isChecked = (if (item.isChecked != null) item.isChecked else false)!!

        holder.childTitle.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                item.isChecked = isChecked
                onDeviceChangeListener(item)
            }
        }
    }

    fun selectAll() {
        for (items in itemList) {
            items.isChecked = true
            onDeviceChangeListener(items)
        }
    }

    fun deselectAll() {
        for (items in itemList) {
            items.isChecked = false
            onDeviceChangeListener(items)
        }
    }
}