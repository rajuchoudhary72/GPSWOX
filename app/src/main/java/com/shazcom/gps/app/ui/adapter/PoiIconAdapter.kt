package com.shazcom.gps.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.IconItems
import com.shazcom.gps.app.databinding.ItemPoiIconBinding


class PoiIconAdapter(
    private val poiPoints: List<IconItems>,
    private val clickListener: (IconItems) -> Unit
) :
    RecyclerView.Adapter<PoiIconAdapter.PoiIconViewHolder>() {

    class PoiIconViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val poiImage = ItemPoiIconBinding.bind(view).poiIcon!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoiIconViewHolder {

        return PoiIconViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_poi_icon,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = poiPoints.size

    override fun onBindViewHolder(holder: PoiIconViewHolder, position: Int) {
        val item = poiPoints[position]
        Glide.with(holder.itemView.context).load(item.url).into(holder.poiImage)
        holder.poiImage.setOnClickListener {
            clickListener(item)
        }
    }
}