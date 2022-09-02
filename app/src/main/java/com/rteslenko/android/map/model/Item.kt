package com.rteslenko.android.map.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import java.lang.Exception

class Item(
    private val position: LatLng,
    private val title: String,
    private val snippet: String
) : ClusterItem {
    override fun getPosition() = position

    override fun getTitle() = title

    override fun getSnippet() = snippet

    companion object {
        fun from(stringArray: Array<String>): Item? {
            val title = stringArray[0]
            val snippet = stringArray[1]
            val lat = stringArray[2]
            val lng = stringArray[3]
            val position = try {
                LatLng(lat.toDouble(), lng.toDouble())
            } catch (e: Exception) {
                return null
            }
            return Item(position, title, snippet)
        }
    }
}