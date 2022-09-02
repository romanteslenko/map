package com.rteslenko.android.map

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLngBounds
import com.rteslenko.android.map.interactors.LoadDataUseCase
import com.rteslenko.android.map.model.Item
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {

    private val data = mutableListOf<Item>()

    private val _isDataReady = MutableLiveData(false)
    val isDataReady: LiveData<Boolean> = _isDataReady

    private val _regionData = MutableLiveData<List<Item>>()
    val regionData: LiveData<List<Item>> = _regionData

    private val _padding = MutableLiveData<Pair<Int, Int>>(0 to 0)
    val padding: LiveData<Pair<Int, Int>> = _padding

    fun loadItems(context: Context) {
        if (data.isEmpty()) {
            viewModelScope.launch {
                data.addAll(LoadDataUseCase().loadData(context))
                _isDataReady.postValue(true)
            }
        }
    }

    fun getDataForRegion(bounds: LatLngBounds) {
        viewModelScope.launch {
            val subdata = data.filter { item -> item.position in bounds }
            _regionData.postValue(subdata)
        }
    }

    fun notifyPaddingChanged(topPadding: Int, bottomPadding: Int) {
        _padding.value = topPadding to bottomPadding
    }
}