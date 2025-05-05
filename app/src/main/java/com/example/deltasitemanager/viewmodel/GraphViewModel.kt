package com.example.deltasitemanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deltasitemanager.models.GraphDataItem
import com.example.deltasitemanager.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

class GraphViewModel : ViewModel() {

    private val _graphData = MutableStateFlow<List<GraphDataItem>>(emptyList())
    val graphData: StateFlow<List<GraphDataItem>> = _graphData

    fun fetchGraphData(macId: String, date: String) {
        val apiKey = ApiClient.apiKey ?: return

        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getGraphInfo(apiKey, macId, date)
                if (response.isSuccessful && response.body()?.status == "success") {
                    _graphData.value = response.body()?.message ?: emptyList()
                    Log.d("GraphViewModel", "Graph data fetched: ${_graphData.value}")
                } else {
                    Log.e("GraphViewModel", "API error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("GraphViewModel", "Error fetching graph data", e)
            }
        }
    }


    fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}
