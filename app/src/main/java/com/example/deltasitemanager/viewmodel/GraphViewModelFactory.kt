package com.example.deltasitemanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GraphViewModelFactory(
    private val authViewModel: AuthViewModel,
    private val macId: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GraphViewModel::class.java)) {
            return GraphViewModel(authViewModel, macId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
