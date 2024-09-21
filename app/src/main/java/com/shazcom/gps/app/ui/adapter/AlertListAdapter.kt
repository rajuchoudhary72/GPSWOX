package com.shazcom.gps.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.AlertData

import kotlinx.android.synthetic.main.item_group.view.*


class AlertListAdapter(
    private val alerts: List<AlertData>,
    private val editListener: (AlertData) -> Unit, private val deleteListener: (AlertData) -> Unit
) :
    RecyclerView.Adapter<AlertListAdapter.AlertListViewHolder>() {

    class AlertListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val groupName = view.groupName!!
        val btnLayout = view.btnLayout!!
        val icDelete = view.deleteAlert!!
        val icEdit = view.editAlert!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertListViewHolder {
        return AlertListViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_group,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = alerts.size

    override fun onBindViewHolder(holder: AlertListViewHolder, position: Int) {
        val item = alerts[position]
        holder.groupName.text = item.name
        holder.btnLayout.visibility = View.VISIBLE

        holder.icDelete.setOnClickListener {
            deleteListener(alerts[position])
        }

        holder.icEdit.setOnClickListener {
            editListener(alerts[position])
        }
    }
}