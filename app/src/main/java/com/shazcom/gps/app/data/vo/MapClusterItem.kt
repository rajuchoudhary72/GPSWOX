package com.shazcom.gps.app.data.vo

import com.shazcom.gps.app.data.response.Items
import com.google.android.libraries.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem


class MapClusterItem : ClusterItem {

    private var title: String = ""
    override fun getTitle(): String {
        return title
    }

    private var position: LatLng? = null
    override fun getPosition(): LatLng? {
        return position!!
    }

    private var snippet: String = ""
    override fun getSnippet(): String {
        return snippet
    }

    fun setColor(color: String) {
        this.color = color
    }

    private var color: String = ""
    fun getColor(): String {
        return color
    }

    private var itemData: Items? = null
    fun setItem(itemData: Items) {
        this.itemData = itemData
    }

    fun getItem(): Items? {
        return this.itemData
    }

    constructor(lat: Double, lng: Double) {
        position = LatLng(lat, lng)
    }


    constructor(lat: Double, lng: Double, title: String, snippet: String) {
        position = LatLng(lat, lng)
        this.title = title
        this.snippet = snippet
    }
}