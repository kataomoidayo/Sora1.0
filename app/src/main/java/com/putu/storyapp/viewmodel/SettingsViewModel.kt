package com.putu.storyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.putu.storyapp.extra.UserPreferences
import kotlinx.coroutines.launch

class SettingsViewModel(private val pref: UserPreferences): ViewModel() {

    fun logout() {
        viewModelScope.launch {
            pref.logout()
        }
    }
}