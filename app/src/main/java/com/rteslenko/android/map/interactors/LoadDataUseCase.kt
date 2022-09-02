package com.rteslenko.android.map.interactors

import android.content.Context
import com.rteslenko.android.map.data.MarkerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoadDataUseCase {

    private val repo by lazy { MarkerRepository() }

    suspend fun loadData(context: Context) = withContext(Dispatchers.IO) {
        repo.getMarkersFromScv(context)
    }
}