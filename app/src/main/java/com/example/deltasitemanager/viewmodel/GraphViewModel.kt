package com.example.deltasitemanager.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deltasitemanager.network.ApiClient
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class GraphViewModel(
    private val authViewModel: AuthViewModel
) : ViewModel() {

    private val _pcsData = MutableStateFlow<List<Entry>>(emptyList())
    val pcsData: StateFlow<List<Entry>> = _pcsData

    private val _gridData = MutableStateFlow<List<Entry>>(emptyList())
    val gridData: StateFlow<List<Entry>> = _gridData

    private val _loadData = MutableStateFlow<List<Entry>>(emptyList())
    val loadData: StateFlow<List<Entry>> = _loadData

    private val _dgData = MutableStateFlow<List<Entry>>(emptyList())
    val dgData: StateFlow<List<Entry>> = _dgData

    private var timeCounter = 0f

    init {
        viewModelScope.launch {
            authViewModel.apiKey
                .combine(authViewModel.siteInfo) { apiKey, siteList ->
                    // Safe call to check for null or empty siteList
                    if (!apiKey.isNullOrBlank() && !siteList.isNullOrEmpty()) {
                        // Safe call to access the first element of siteList
                        Pair(apiKey, siteList.firstOrNull()?.mac_id)
                    } else null
                }
                .filterNotNull()  // Filters out null pairs
                .collect { (apiKey, macId) ->
                    // Handle real-time data fetching every 60 seconds
                    while (true) {
                        macId?.let {
                            fetchRealTimeData(apiKey, it)
                        }
                        delay(60_000) // fetch every 60 seconds
                    }
                }
        }
    }


    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    private suspend fun fetchRealTimeData(apiKey: String, macId: String) {
        try {
            val response = ApiClient.apiService.getIndividualSiteInfo(apiKey, macId)
            if (response.isSuccessful && response.body()?.status == "success") {
                val siteInfo = response.body()?.message?.getOrNull(0)
                siteInfo?.let {
                    val timestamp = dateFormat.parse(it.evtime)?.time?.toFloat() ?: return@let

                    _pcsData.value += Entry(timestamp, it.PCS_ActivePower.toFloat())
                    _gridData.value += Entry(timestamp, it.GRID_Active_Power_RYB.toFloat())
                    _loadData.value += Entry(timestamp, it.Load_Active_Power.toFloat())
                    _dgData.value += Entry(timestamp, it.DG1_Active_Power_RYB.toFloat())
                }
            } else {
                Log.e("GraphViewModel", "Failed response: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("GraphViewModel", "Exception: ${e.localizedMessage}")
        }
    }


}
