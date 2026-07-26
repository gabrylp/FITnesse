package com.fitnesse.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnesse.app.data.model.ClothingItem
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
    val selectedOutfitItems: List<ClothingItem> = emptyList(),
    val showOutfitDetail: Boolean = false,
    val isPreview: Boolean = false,
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

    fun onDayClicked(dateStr: String) {
        viewModelScope.launch {
            val saved = _state.value.outfits.find { it.date == dateStr }
            if (saved != null) {
                val items = repository.resolveOutfitItems(saved.items)
                _state.value = _state.value.copy(
                    selectedOutfit = saved,
                    selectedOutfitItems = items,
                    showOutfitDetail = true,
                    isPreview = false,
                )
            } else {
                val preview = repository.generateOutfitPreview(dateStr)
                if (preview != null) {
                    val items = repository.resolveOutfitItems(preview.items)
                    _state.value = _state.value.copy(
                        selectedOutfit = preview,
                        selectedOutfitItems = items,
                        showOutfitDetail = true,
                        isPreview = true,
                    )
                }
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
                isPreview = false,
            )
        }
    }

    fun hideOutfitDetail() {
        _state.value = _state.value.copy(showOutfitDetail = false, selectedOutfit = null, selectedOutfitItems = emptyList(), isPreview = false)
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
