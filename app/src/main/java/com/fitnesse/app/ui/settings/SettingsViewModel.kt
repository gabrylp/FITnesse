package com.fitnesse.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnesse.app.data.model.UserSettings
import com.fitnesse.app.data.repository.WardrobeRepository
import com.fitnesse.app.ui.theme.ThemeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    val email: String = "",
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
                email = repository.getCurrentUserEmail(),
                isLoading = false,
            )
        }
    }

    private val defaultCooldownCategories = listOf("top", "bottom", "outerwear")

    fun toggleCooldown(enabled: Boolean) {
        viewModelScope.launch {
            val updated = _state.value.settings.copy(laundryCooldownEnabled = enabled)
            _state.value = _state.value.copy(settings = updated)
            repository.saveUserSettings(updated)
            if (!enabled) {
                val allItems = repository.getClothingItems()
                allItems.filter { it.lastWorn > 0L }.forEach {
                    repository.updateClothingItem(it.copy(lastWorn = 0L))
                }
            }
        }
    }

    fun toggleCooldownCategory(category: String) {
        viewModelScope.launch {
            val current = _state.value.settings.cooldownCategories
            val updated = if (category.lowercase() in current.map { it.lowercase() }) {
                current.filter { it.lowercase() != category.lowercase() }
            } else {
                current + category.lowercase()
            }.ifEmpty { defaultCooldownCategories }
            val settings = _state.value.settings.copy(cooldownCategories = updated)
            _state.value = _state.value.copy(settings = settings)
            repository.saveUserSettings(settings)
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
        ThemeManager.theme = theme
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
