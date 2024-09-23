package com.shazcom.gps.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.AlertData
import com.shazcom.gps.app.databinding.ItemAlertDeviceBinding
import com.shazcom.gps.app.databinding.ItemGroupBinding




class AlertListAdapter(
    private val alerts: List<AlertData>,
    private val editListener: (AlertData) -> Unit, private val deleteListener: (AlertData) -> Unit
) :
    RecyclerView.Adapter<AlertListAdapter.AlertListViewHolder>() {

    class AlertListViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val binding=ItemGroupBinding.bind(view)
        val groupName = binding.groupName!!
        val btnLayout =binding.btnLayout!!
        val icDelete = binding.deleteAlert!!
        val icEdit = binding.editAlert!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertListViewHolder {
        val item =
            ItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return AlertListViewHolder(
            item.rootView
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