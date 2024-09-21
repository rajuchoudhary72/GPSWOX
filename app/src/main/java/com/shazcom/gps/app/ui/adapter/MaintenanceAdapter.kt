package com.shazcom.gps.app.ui.adapter

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.Services
import com.shazcom.gps.app.utils.differenceInDay
import kotlinx.android.synthetic.main.item_maintenance.view.*


class MaintenanceAdapter(
    events: List<Services>
) :
    RecyclerView.Adapter<MaintenanceAdapter.MaintenanceViewHolder>() {

    private var dataItems: MutableList<Services>? = null

    init {
        dataItems = events as MutableList<Services>
    }

    fun addNewItems(data: List<Services>) {
        dataItems?.addAll(data)
        notifyDataSetChanged()
    }

    class MaintenanceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val serviceName = view.serviceName!!
        val serviceDue = view.timeLeft!!
        val deviceName = view.deviceName!!
        var progressBar = view.progress_limit!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaintenanceViewHolder {
        return MaintenanceViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_maintenance,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = dataItems?.size!!

    override fun onBindViewHolder(holder: MaintenanceViewHolder, position: Int) {

        val item = dataItems?.get(position)
        holder.deviceName.text = item?.deviceName

        val interval = item?.interval ?: 0
        val expiryDate = item?.expires_date ?: ""

        var days = 0

        if (expiryDate.isNotEmpty()) {
            days = differenceInDay(expiryDate)
        }



        try {
            val percentage = (days * 100 )/ interval
            val resources = holder.progressBar.context

            val htmlTxt = "<b><font color='#ffa500'>${item?.name}</font></b> $days d. left ($percentage%)"
            holder.serviceName.setText(Html.fromHtml(htmlTxt), TextView.BufferType.SPANNABLE)

            println("value data : $interval $expiryDate $days $percentage%")
            when {
                percentage >= 50 -> {
                    holder.progressBar.progress = percentage
                    DrawableCompat.setTint(
                        holder.progressBar.indeterminateDrawable,
                        ContextCompat.getColor(resources, R.color.color_green)
                    )
                }
                percentage in 25..49 -> {
                    holder.progressBar.progress = percentage
                    DrawableCompat.setTint(
                        holder.progressBar.indeterminateDrawable,
                        ContextCompat.getColor(resources, R.color.color_orange)
                    )
                }
                else -> {
                    holder.progressBar.progress = percentage
                    DrawableCompat.setTint(
                        holder.progressBar.indeterminateDrawable,
                        ContextCompat.getColor(resources, R.color.color_danger)
                    )
                }
            }

            holder.progressBar.visibility = View.VISIBLE
            holder.progressBar.invalidate()

        } catch (ex: Exception) {
            holder.progressBar.visibility = View.GONE
        }


        /* item?.value?.let {
             holder.serviceDue.text = item?.value
             holder.serviceDue.visibility = View.VISIBLE
         } ?: kotlin.run {
             holder.serviceDue.visibility = View.GONE
         }*/

    }
}