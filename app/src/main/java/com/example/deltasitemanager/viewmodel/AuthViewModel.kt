package com.example.deltasitemanager.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deltasitemanager.models.GraphDataItem
import com.example.deltasitemanager.models.IndividualSiteInfo
import com.example.deltasitemanager.models.SiteInfo
import com.example.deltasitemanager.network.ApiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class AuthViewModel : ViewModel() {

    private val _apiKey = MutableStateFlow<String?>(null)
    val apiKey: StateFlow<String?> = _apiKey

    private val _siteInfo = MutableStateFlow<List<SiteInfo>?>(null)
    val siteInfo: StateFlow<List<SiteInfo>?> = _siteInfo

    private val _individualSiteInfo = MutableStateFlow<List<IndividualSiteInfo>?>(null)
    val individualSiteInfo: StateFlow<List<IndividualSiteInfo>?> = _individualSiteInfo

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _selectedMacId = MutableStateFlow<String?>(null)
    val selectedMacId: StateFlow<String?> = _selectedMacId

    private var autoRefreshJob: Job? = null

    // Login function
    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val usernamePart = username.toRequestBody("text/plain".toMediaType())
                val passwordPart = password.toRequestBody("text/plain".toMediaType())

                val response = ApiClient.apiService.login(usernamePart, passwordPart)
                if (response.status == "success") {
                    ApiClient.apiKey = response.api_key
                    _apiKey.value = response.api_key
                    // Start auto-refresh after login success
                    startAutoRefresh()
                } else {
                    _error.value = safeMessage(response.message, "Login failed")
                }
            } catch (e: Exception) {
                _error.value = "Login failed: ${e.localizedMessage}"
            }
        }
    }

    // Set the selected MAC ID
    fun setSelectedMacId(macId: String) {
        _selectedMacId.value = macId
    }

    // Periodic auto-refresh logic
    private fun startAutoRefresh() {
        autoRefreshJob = viewModelScope.launch {
            while (true) {
                delay(60_000) // Refresh every 60 seconds
                fetchLatestData()
            }
        }
    }

    private suspend fun fetchLatestData() {
        val selectedMacId = _selectedMacId.value
        if (selectedMacId != null) {
            getSiteInfo()
            getIndividualSiteInfo(selectedMacId)
        } else {
            _error.value = "No MAC ID selected for data refresh"
        }
    }

    // Fetch site info
    fun getSiteInfo() {
        viewModelScope.launch {
            val key = requireApiKey() ?: return@launch

            try {
                val response = ApiClient.apiService.getSiteInfo(apiKey = key)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status == "success") {
                        val siteList = parseJsonArray<SiteInfo>(body.message)
                        if (siteList != null) {
                            _siteInfo.value = siteList
                        } else {
                            _error.value = "Invalid site data format"
                        }
                    } else {
                        _error.value = body?.message?.toString() ?: "No site data available"
                    }
                } else {
                    _error.value = "API call failed: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error fetching site info: ${e.localizedMessage}"
            }
        }
    }

    // Fetch individual site info
    fun getIndividualSiteInfo(macId: String) {
        viewModelScope.launch {
            val key = requireApiKey() ?: return@launch

            try {
                val response = ApiClient.apiService.getIndividualSiteInfo(apiKey = key, macId = macId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "success" && body.message is List<*>) {
                        _individualSiteInfo.value = body.message.filterIsInstance<IndividualSiteInfo>()
                    } else {
                        _error.value = "Invalid or empty site data"
                    }
                } else {
                    _error.value = "Error: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Exception: ${e.localizedMessage}"
            }
        }
    }

    // Helper to safely extract a message
    private fun safeMessage(message: String?, fallback: String): String {
        return message ?: fallback
    }

    // Helper to require API Key or abort
    private fun requireApiKey(): String? {
        val key = _apiKey.value
        if (key.isNullOrBlank()) {
            _error.value = "API Key is missing"
            return null
        }
        return key
    }

    // Helper to parse generic JSON arrays using Gson
    private inline fun <reified T> parseJsonArray(json: Any): List<T>? {
        return try {
            val gson = Gson()
            val jsonElement = gson.toJsonTree(json)
            if (jsonElement.isJsonArray) {
                gson.fromJson(jsonElement, object : TypeToken<List<T>>() {}.type)
            } else null
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Failed to parse JSON: ${e.message}")
            null
        }
    }

    // Clear session data
    fun clearSession() {
        _apiKey.value = null
        _siteInfo.value = null
        _individualSiteInfo.value = null
        _selectedMacId.value = null
        _error.value = null
        autoRefreshJob?.cancel()
    }
}
