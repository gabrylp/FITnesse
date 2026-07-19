package com.fitnesse.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnesse.app.data.model.OutfitRecommendation
import com.fitnesse.app.data.repository.WardrobeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CalendarUiState(
    val outfits: List<OutfitRecommendation> = emptyList(),
    val isLoading: Boolean = false,
    val selectedOutfit: OutfitRecommendation? = null,
    val selectedOutfitItems: List<com.fitnesse.app.data.model.ClothingItem> = emptyList(),
    val showOutfitDetail: Boolean = false,
)

class CalendarViewModel(
    private val repository: WardrobeRepository = WardrobeRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(CalendarUiState())
    val state: StateFlow<CalendarUiState> = _state.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val outfits = repository.getOutfitHistory()
                _state.value = _state.value.copy(outfits = outfits, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun showOutfitDetail(outfit: OutfitRecommendation) {
        viewModelScope.launch {
            val items = repository.resolveOutfitItems(outfit.items)
            _state.value = _state.value.copy(
                selectedOutfit = outfit,
                selectedOutfitItems = items,
                showOutfitDetail = true,
            )
        }
    }

    fun hideOutfitDetail() {
        _state.value = _state.value.copy(showOutfitDetail = false, selectedOutfit = null, selectedOutfitItems = emptyList())
    }

    fun deleteOutfit(outfitId: String) {
        viewModelScope.launch {
            try {
                repository.deleteOutfit(outfitId)
                hideOutfitDetail()
                loadHistory()
            } catch (_: Exception) { }
        }
    }
}
