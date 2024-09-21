package com.shazcom.gps.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.ReportData
import kotlinx.android.synthetic.main.item_group.view.*


class ReportListAdapter(
    private val alerts: List<ReportData>,
    private val editListener: (ReportData) -> Unit, private val deleteListener: (ReportData) -> Unit
) :
    RecyclerView.Adapter<ReportListAdapter.ReportListViewHolder>() {

    class ReportListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val groupName = view.groupName!!
        val btnLayout = view.btnLayout!!
        val icDelete = view.deleteAlert!!
        val icEdit = view.editAlert!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportListViewHolder {
        return ReportListViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_group,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = alerts.size

    override fun onBindViewHolder(holder: ReportListViewHolder, position: Int) {
        val item = alerts[position]
        holder.groupName.text = item.title
        holder.btnLayout.visibility = View.VISIBLE

        holder.icDelete.setOnClickListener {
            deleteListener(alerts[position])
        }

        holder.icEdit.setOnClickListener {
            editListener(alerts[position])
        }
    }
}