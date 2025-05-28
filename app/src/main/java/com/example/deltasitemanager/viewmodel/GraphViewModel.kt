package com.example.deltasitemanager.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deltasitemanager.models.GraphDataItem
import com.example.deltasitemanager.models.IndividualSiteInfo
import com.example.deltasitemanager.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.Calendar


class GraphViewModel : ViewModel() {

    private val _graphData = MutableStateFlow<List<GraphDataItem>>(emptyList())
    val graphData: StateFlow<List<GraphDataItem>> = _graphData

    private val _siteInfo = MutableStateFlow<List<IndividualSiteInfo>?>(null)
    val siteInfo: StateFlow<List<IndividualSiteInfo>?> = _siteInfo

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchGraphData(macId: String, date: String) {
        val apiKey = getApiKeyOrLogError() ?: return

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)!!

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(calendar.time)

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(calendar.time)

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = ApiClient.apiService.getGraphInfo(apiKey, macId, date, startTime, endTime)
                val body = response.body()
                if (response.isSuccessful && body?.status == "success") {
                    _graphData.value = body.message ?: emptyList()
                    logDebug("Graph data fetched successfully: ${_graphData.value.size} items")
                } else {
                    handleError(body?.message?.toString() ?: "Failed to fetch graph data")
                }
            } catch (e: Exception) {
                handleError("Exception in fetchGraphData: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }




    fun fetchSiteInfo(macId: String) {
        val apiKey = getApiKeyOrLogError() ?: return

        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getIndividualSiteInfo(apiKey, macId)
                if (response.isSuccessful) {
                    _siteInfo.value = response.body()?.message
                    logDebug("Site info fetched: ${_siteInfo.value?.size ?: 0} records")
                } else {
                    handleError("Failed to fetch site info: ${response.code()}")
                }
            } catch (e: Exception) {
                handleError("Exception in fetchSiteInfo: ${e.localizedMessage}")
            }
        }
    }

    fun getTodayDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private fun getApiKeyOrLogError(): String? {
        val key = ApiClient.apiKey
        return if (key.isNullOrBlank()) {
            handleError("API Key is missing")
            null
        } else key
    }

    private fun handleError(message: String) {
        _error.value = message
        logError(message)
    }

    private fun logDebug(msg: String) = Log.d("GraphViewModel", msg)

    private fun logError(msg: String) = Log.e("GraphViewModel", msg)
}
