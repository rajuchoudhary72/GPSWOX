package com.shazcom.gps.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.MapIcons
import com.shazcom.gps.app.databinding.ItemUserPoiBinding


class UserPoiAdapter(
    private val poiPoints: List<MapIcons>,
    private val clickListener: (MapIcons) -> Unit,
    private val deleteClickListener: (MapIcons) -> Unit,
    private val editClickListener: (MapIcons) -> Unit
) :
    RecyclerView.Adapter<UserPoiAdapter.UserPoiViewHolder>() {

    class UserPoiViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val poiImage = ItemUserPoiBinding.bind(view).poiImage!!
        val poiTitle =  ItemUserPoiBinding.bind(view).poiTitle!!
        val poiDescription =  ItemUserPoiBinding.bind(view).poiDescription!!
        val rootView =  ItemUserPoiBinding.bind(view).rootView!!
        val btnLayout =  ItemUserPoiBinding.bind(view).btnLayout!!
        val editBtn =  ItemUserPoiBinding.bind(view).editBtn!!
        val deleteBtn =  ItemUserPoiBinding.bind(view).deleteBtn!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserPoiViewHolder {
        return UserPoiViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_user_poi,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = poiPoints.size

    override fun onBindViewHolder(holder: UserPoiViewHolder, position: Int) {
        val item = poiPoints[position]
        Glide.with(holder.itemView.context).load(item.map_icon.url).into(holder.poiImage)
        holder.poiTitle.text = "${item.name}"
        holder.poiDescription.text = "${item.description}"
        holder.btnLayout.visibility = View.VISIBLE

        holder.rootView.setOnClickListener {
            clickListener(item)
        }

        holder.editBtn.setOnClickListener { editClickListener(item) }
        holder.deleteBtn.setOnClickListener { deleteClickListener(item) }
    }
}