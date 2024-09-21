package com.shazcom.gps.app.ui.adapter

import android.content.Intent
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.ServiceData
import com.shazcom.gps.app.ui.activities.AddServicePage
import com.shazcom.gps.app.ui.activities.ServicePage
import kotlinx.android.synthetic.main.item_service.view.*

class ServiceAdapter(
    private val activity : ServicePage,
    events: List<ServiceData>,
    private val deviceName: String,
    private val odometerValue: String,
    private val engineHrs: String
) :
    RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    private var dataItems: MutableList<ServiceData>? = null

    init {
        dataItems = events as MutableList<ServiceData>
    }

    fun addNewItems(data: List<ServiceData>) {
        dataItems?.addAll(data)
        notifyDataSetChanged()
    }

    class ServiceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val serviceName = view.serviceName!!
        val serviceDue = view.timeLeft!!
        val edtBtn = view.edtBtn!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        return ServiceViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_service,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = dataItems?.size!!

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val item = dataItems?.get(position)
        val htmlTxt = "<b><font color='#ffa500'>${item?.name}</font></b> ${item?.expires}"
        holder.serviceName.setText(Html.fromHtml(htmlTxt), TextView.BufferType.SPANNABLE)

        item?.expires_date?.let {
            holder.serviceDue.text = item?.expires_date
            holder.serviceDue.visibility = View.VISIBLE
        } ?: kotlin.run {
            holder.serviceDue.visibility = View.GONE
        }

        holder.edtBtn.visibility = View.VISIBLE
        holder.edtBtn.setOnClickListener {
            val itemData = dataItems?.get(position)
            Intent(activity , AddServicePage::class.java).apply {
                putExtra("deviceId", item?.device_id)
                putExtra("deviceName", deviceName)
                putExtra("odometer", odometerValue)
                putExtra("engineLoad", engineHrs)
                putExtra("item", itemData)
                activity.startActivityForResult(this, 11230)
            }
        }
    }
}