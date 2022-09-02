package com.rteslenko.android.map.utils

import android.content.Context
import android.util.Log
import android.util.SparseArray
import androidx.annotation.RawRes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.algo.NonHierarchicalViewBasedAlgorithm
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.rteslenko.android.map.R
import com.rteslenko.android.map.model.Item

object GoogleMapHelper {

    private val kyiv = LatLng(50.51261983394099, 30.444805904614345)
    private const val defaultZoom = 8f

    fun GoogleMap.moveCameraToKyiv() {
        moveCamera(CameraUpdateFactory.newLatLngZoom(kyiv, defaultZoom))
    }

    fun GoogleMap.setMapStyleFromRes(context: Context, @RawRes styleRes: Int) {
        try {
            setMapStyle(
                MapStyleOptions.loadRawResourceStyle(context, styleRes)
            )
        } catch (e: Exception) {
            Log.e(GoogleMapHelper.javaClass.simpleName, "Cannot apply map style: $e")
        }
    }

    fun GoogleMap.getVisibleBounds(): LatLngBounds = projection.visibleRegion.latLngBounds

    fun GoogleMap.setupItemClusterManager(context: Context) : ClusterManager<Item> {
        val displayMetrics = context.resources.displayMetrics
        val dpWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()
        val dpHeight = (displayMetrics.heightPixels / displayMetrics.density).toInt()

        return ClusterManager<Item>(context, this).apply {
            algorithm = NonHierarchicalViewBasedAlgorithm(dpWidth, dpHeight) // slow but pretty
            // algorithm = GridBasedAlgorithm() // faster but visually not great

            // There are 7 different cluster icon options in DefaultClusterRenderer
            // plus icon for individual marker.
            // Only 4 different icons needed for the app so rendered has to be modded
            renderer = object : DefaultClusterRenderer<Item>(context,
                this@setupItemClusterManager,
                this
            ) {
                private val icons by lazy {
                    SparseArray<BitmapDescriptor>().apply {
                        put(0, BitmapDescriptorFactory.fromResource(R.drawable.wifi_24))
                        put(1, BitmapDescriptorFactory.fromResource(R.drawable.wifi_36))
                        put(2, BitmapDescriptorFactory.fromResource(R.drawable.wifi_48))
                        put(3, BitmapDescriptorFactory.fromResource(R.drawable.wifi_72))
                    }
                }

                override fun getBucket(cluster: Cluster<Item>): Int {
                    return when {
                        cluster.size == 1 -> 0
                        cluster.size in 2..49 -> 1
                        cluster.size in 49..199 -> 2
                        else -> 3
                    }
                }

                override fun getDescriptorForCluster(cluster: Cluster<Item>): BitmapDescriptor {
                    val bucket = getBucket(cluster)
                    return icons[bucket]
                }

                override fun onBeforeClusterItemRendered(item: Item, markerOptions: MarkerOptions) {
                    super.onBeforeClusterItemRendered(item, markerOptions)
                    markerOptions.icon(icons[0])
                }
            }
        }
    }
}