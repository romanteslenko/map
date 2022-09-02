package com.rteslenko.android.map.data

import android.content.Context
import android.util.Log
import com.opencsv.CSVReader
import com.rteslenko.android.map.R
import com.rteslenko.android.map.model.Item
import java.io.InputStreamReader
import java.lang.Exception

class MarkerRepository {

    fun getMarkersFromScv(context: Context): List<Item> {
        val data = mutableListOf<Item>()
        try {
            context.resources.openRawResource(R.raw.hotspots).use { stream ->
                CSVReader(InputStreamReader(stream)).use { reader ->
                    reader.readNext() // skip csv header
                    var next = reader.readNext()
                    while (next != null) {
                        Item.from(next)?.let { item ->
                            data.add(item)
                        }
                        next = reader.readNext()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Roman", "Cannot load data from csv file: $e")
        }
        return data
    }
}