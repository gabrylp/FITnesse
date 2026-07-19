package com.fitnesse.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnesse.app.data.model.UserSettings
import com.fitnesse.app.data.repository.WardrobeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
)

class SettingsViewModel(
    private val repository: WardrobeRepository = WardrobeRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val settings = repository.getUserSettings()
            _state.value = _state.value.copy(
                settings = settings,
                isSignedIn = repository.isSignedIn(),
                isLoading = false,
            )
        }
    }

    fun toggleCooldown(enabled: Boolean) {
        viewModelScope.launch {
            val updated = _state.value.settings.copy(laundryCooldownEnabled = enabled)
            _state.value = _state.value.copy(settings = updated)
            repository.saveUserSettings(updated)
        }
    }

    fun setCooldownDays(days: Int) {
        viewModelScope.launch {
            val updated = _state.value.settings.copy(cooldownDays = days)
            _state.value = _state.value.copy(settings = updated)
            repository.saveUserSettings(updated)
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            val updated = _state.value.settings.copy(theme = theme)
            _state.value = _state.value.copy(settings = updated)
            repository.saveUserSettings(updated)
        }
    }

    fun signOut() {
        repository.signOut()
        _state.value = _state.value.copy(isSignedIn = false)
    }
}
