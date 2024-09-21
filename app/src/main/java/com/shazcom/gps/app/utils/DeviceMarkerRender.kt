package com.shazcom.gps.app.utils

import android.content.Context
import android.util.Log
import com.shazcom.gps.app.data.vo.MapClusterItem
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.maps.model.Marker
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator


class DeviceMarkerRender(
    private val context: Context,
    private val map: GoogleMap,
    private val clusterManager: ClusterManager<MapClusterItem>
) : DefaultClusterRenderer<MapClusterItem>(context, map, clusterManager) {

    var clusterMarkerMap= HashMap<Cluster<MapClusterItem>, Marker>()
    override fun onBeforeClusterItemRendered(item: MapClusterItem, markerOptions: MarkerOptions) {
        val deviceIcon = getCar(item.getColor())
        markerOptions.icon(BitmapDescriptorFactory.fromResource(deviceIcon))
    }


    override fun onClusterRendered(cluster: Cluster<MapClusterItem>?, marker: Marker?) {
        super.onClusterRendered(cluster, marker)
        clusterMarkerMap[cluster!!] = marker!!
    }

    override fun onBeforeClusterRendered(
        cluster: Cluster<MapClusterItem>,
        markerOptions: MarkerOptions
    ) {
        val bucket = getBucket(cluster)
        Log.e("Bucket", "$bucket")
        val iconGenerator = IconGenerator(this@DeviceMarkerRender.context)
        val bitmap = iconGenerator.makeIcon(cluster.size.toString())
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
    }

    override fun shouldRenderAsCluster(cluster: Cluster<MapClusterItem>): Boolean {
        return cluster.size > 1
    }

    override fun getBucket(cluster: Cluster<MapClusterItem>?): Int {
        return cluster?.size!!
    }

    override fun onClusterItemRendered(clusterItem: MapClusterItem?, marker: Marker?) {
        super.onClusterItemRendered(clusterItem, marker)
        marker?.showInfoWindow()
    }

    fun getMarkerItem(cluster: Cluster<MapClusterItem>): Marker? {
        return clusterMarkerMap.get(cluster)
    }

    override fun setAnimation(animate: Boolean) {
        super.setAnimation(true)
    }
}