package com.shazcom.gps.app.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.TaskData
import com.shazcom.gps.app.databinding.ItemTaskListBinding

class TaskAdapter(private val tasks: List<TaskData>) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTxt =ItemTaskListBinding.bind( view).title!!
        val invoiceNo = ItemTaskListBinding.bind( view).invoice!!
        val comment = ItemTaskListBinding.bind( view).commentTxt!!
        val pickupTime = ItemTaskListBinding.bind( view).pickUpTime!!
        val delTime = ItemTaskListBinding.bind( view).delTime!!
        val pickLocation = ItemTaskListBinding.bind( view).pickLocation!!
        val delLocation = ItemTaskListBinding.bind( view).dropLocation!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_task_list,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = tasks?.size!!

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val item = tasks?.get(position)
        if (item != null) {
            holder.titleTxt.text = item.title
            holder.invoiceNo.text = item.invoice_number
            holder.comment.text = item.comment
            holder.pickupTime.text = "${item.pickup_time_from}\n${item.pickup_time_to}"
            holder.delTime.text = "${item.delivery_time_from}\n${item.delivery_time_to}"
            holder.pickLocation.text = item.pickup_address
            holder.delLocation.text = item.delivery_address
        }

    }
}