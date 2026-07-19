package com.fitnesse.app.ui.ootd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnesse.app.data.model.ClothingItem
import com.fitnesse.app.data.model.OutfitRecommendation
import com.fitnesse.app.data.repository.WardrobeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OotdUiState(
    val outfit: OutfitRecommendation? = null,
    val items: List<ClothingItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class OotdViewModel(
    private val repository: WardrobeRepository = WardrobeRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(OotdUiState())
    val state: StateFlow<OotdUiState> = _state.asStateFlow()

    init {
        loadTodaysOutfit()
    }

    fun loadTodaysOutfit() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val outfit = repository.getTodaysOutfit()
                val clothingItems = if (outfit != null) {
                    val all = repository.getClothingItems()
                    all.filter { it.id in outfit.items }
                } else emptyList()
                
                _state.value = _state.value.copy(
                    outfit = outfit,
                    items = clothingItems,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun confirmWorn() {
        val outfit = _state.value.outfit ?: return
        viewModelScope.launch {
            try {
                repository.confirmWorn(outfit.id, outfit.items)
                loadTodaysOutfit()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
}
