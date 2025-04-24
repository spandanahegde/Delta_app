package com.example.deltasitemanager.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deltasitemanager.network.ApiClient
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class GraphViewModel(
    private val authViewModel: AuthViewModel,
    private val macId: String
) : ViewModel() {

    private val _pcsData = MutableStateFlow<List<Entry>>(emptyList())
    val pcsData: StateFlow<List<Entry>> = _pcsData

    private val _gridData = MutableStateFlow<List<Entry>>(emptyList())
    val gridData: StateFlow<List<Entry>> = _gridData

    private val _loadData = MutableStateFlow<List<Entry>>(emptyList())
    val loadData: StateFlow<List<Entry>> = _loadData

    private val _dgData = MutableStateFlow<List<Entry>>(emptyList())
    val dgData: StateFlow<List<Entry>> = _dgData

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    init {
        if (macId.isNotBlank()) {
            viewModelScope.launch {
                try {
                    loadGraph(macId)
                } catch (e: Exception) {
                    Log.e("GraphLoad", "Failed to load graph: ${e.message}", e)
                }
            }
        } else {
            Log.w("GraphViewModel", "macId is blank. Skipping graph load.")
        }
    }

    init {
        viewModelScope.launch {
            try {
                loadGraph(macId)
            } catch (e: Exception) {
                Log.e("GraphLoad", "Failed to load graph: ${e.message}", e)
            }
        }
    }

    private suspend fun loadGraph(macId: String) {
        val apiKey = authViewModel.getApiKey() // <-- Now this works
        while (true) {
            fetchRealTimeData(apiKey, macId)
            delay(5000)
        }
    }


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
