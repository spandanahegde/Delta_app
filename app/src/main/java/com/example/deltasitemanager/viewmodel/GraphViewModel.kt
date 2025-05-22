package com.example.deltasitemanager.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deltasitemanager.models.GraphDataItem
import com.example.deltasitemanager.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class GraphViewModel : ViewModel() {

    private val _graphData = MutableStateFlow<List<GraphDataItem>>(emptyList())
    val graphData: StateFlow<List<GraphDataItem>> = _graphData

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchGraphData(macId: String, date: String) {
        val apiKey = getApiKeyOrLogError() ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = ApiClient.apiService.getGraphInfo(apiKey, macId, date)
                val body = response.body()
                if (response.isSuccessful && body?.status == "success") {
                    _graphData.value = body.message ?: emptyList()
                    logDebug("Graph data fetched: ${_graphData.value}")
                } else {
                    val errorMessage = body?.message?.toString() ?: "API response failed"
                    handleError(errorMessage)
                }
            } catch (e: Exception) {
                handleError("Error fetching graph data: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun getTodayDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private fun getApiKeyOrLogError(): String? {
        val key = ApiClient.apiKey
        if (key.isNullOrBlank()) {
            handleError("API Key is missing")
            return null
        }
        return key
    }
    private fun handleError(message: String) {
        _error.value = message
        logError(message)
    }
    private fun logDebug(msg: String) = Log.d("GraphViewModel", msg)
    private fun logError(msg: String) = Log.e("GraphViewModel", msg)
}
