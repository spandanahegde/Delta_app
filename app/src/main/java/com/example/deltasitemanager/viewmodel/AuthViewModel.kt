package com.example.deltasitemanager.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deltasitemanager.models.IndividualSiteInfo
import com.example.deltasitemanager.models.SiteInfo
import com.example.deltasitemanager.network.ApiClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.UnknownHostException

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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var autoRefreshJob: Job? = null

    /** Performs user login and stores the API key on success */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val usernamePart = username.toRequestBody("text/plain".toMediaType())
                val passwordPart = password.toRequestBody("text/plain".toMediaType())

                val response = ApiClient.apiService.login(usernamePart, passwordPart)
                if (response.status == "success") {
                    ApiClient.apiKey = response.api_key
                    _apiKey.value = response.api_key
                    // Don't start auto-refresh here; wait until MAC ID is set
                } else {
                    _error.value = safeMessage(response.message, "Login failed")
                }
            } catch (e: Exception) {
                _error.value = "Login failed: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Updates the selected MAC ID and starts auto-refresh */
    fun setSelectedMacId(macId: String) {
        _selectedMacId.value = macId
        startAutoRefresh() // Trigger only after MAC ID is available
    }

    /** Fetches site summary info */
    fun getSiteInfo() {
        viewModelScope.launch {
            val key = requireApiKey() ?: return@launch
            try {
                val response = ApiClient.apiService.getSiteInfo(apiKey = key)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status == "success") {
                        _siteInfo.value = parseJsonArray<SiteInfo>(body.message)
                            ?: throw IllegalStateException("Invalid site data format")
                    } else {
                        _error.value = body?.message?.toString() ?: "No site data available"
                    }
                } else {
                    _error.value = "API call failed: ${response.code()}"
                }
            } catch (e: UnknownHostException) {
                Log.e("AuthViewModel", "Network error: ${e.localizedMessage}", e)
                _error.value = "No internet connection or server unreachable"
            } catch (e: Exception) {
                Log.e("AuthViewModel", "General error: ${e.localizedMessage}", e)
                _error.value = "Error fetching site info: ${e.localizedMessage}"
            }
        }
    }

    /** Fetches detailed data for the selected site */
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

    /** Clears user session and stops background tasks */
    fun clearSession() {
        _apiKey.value = null
        _siteInfo.value = null
        _individualSiteInfo.value = null
        _selectedMacId.value = null
        _error.value = null
        autoRefreshJob?.cancel()
    }

    /** Starts periodic refresh if not already running */
    private fun startAutoRefresh() {
        if (autoRefreshJob?.isActive == true) return

        autoRefreshJob = viewModelScope.launch {
            var retryDelay = 60_000L
            while (true) {
                delay(retryDelay)
                val macId = _selectedMacId.value
                if (macId != null) {
                    try {
                        getSiteInfo()
                        getIndividualSiteInfo(macId)
                        retryDelay = 60_000L
                    } catch (e: UnknownHostException) {
                        _error.value = "DNS error: Check internet or server config"
                        retryDelay = 180_000L
                    } catch (e: Exception) {
                        _error.value = "Error during auto-refresh: ${e.localizedMessage}"
                        retryDelay = 120_000L
                    }
                } else {
                    _error.value = "No MAC ID selected. Stopping auto-refresh."
                    autoRefreshJob?.cancel()
                    break
                }
            }
        }
    }

    private fun requireApiKey(): String? {
        return _apiKey.value ?: run {
            _error.value = "API Key is missing"
            null
        }
    }

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

    private fun safeMessage(message: String?, fallback: String): String {
        return message ?: fallback
    }
}
