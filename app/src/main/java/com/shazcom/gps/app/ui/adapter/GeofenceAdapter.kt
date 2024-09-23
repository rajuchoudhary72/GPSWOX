package com.shazcom.gps.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.GeoFenceData
import com.shazcom.gps.app.databinding.ItemGeoFenceBinding


class GeofenceAdapter(
    private val geoPoints: List<GeoFenceData>,
    private val clickListener: (GeoFenceData) -> Unit,
    private val deleteClickListener: (GeoFenceData) -> Unit,
    private val editClickListener: (GeoFenceData) -> Unit
) :
    RecyclerView.Adapter<GeofenceAdapter.PoiIconViewHolder>() {

    class PoiIconViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name =ItemGeoFenceBinding.bind( view).name!!
        val description = ItemGeoFenceBinding.bind( view).description!!
        val btnLayout = ItemGeoFenceBinding.bind( view).btnLayout!!
        val editBtn = ItemGeoFenceBinding.bind( view).editBtn!!
        val deleteBtn = ItemGeoFenceBinding.bind( view).deleteBtn!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoiIconViewHolder {
        return PoiIconViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_geo_fence,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = geoPoints.size

    override fun onBindViewHolder(holder: PoiIconViewHolder, position: Int) {
        val item = geoPoints[position]
        holder.name.text = item.name
        holder.description.text = item.updated_at
        holder.btnLayout.visibility = View.VISIBLE

        holder.editBtn.setOnClickListener {
            editClickListener(item)
        }

        holder.deleteBtn.setOnClickListener {
            deleteClickListener(item)
        }

        holder.itemView.setOnClickListener {
            clickListener(item)
        }
    }
}