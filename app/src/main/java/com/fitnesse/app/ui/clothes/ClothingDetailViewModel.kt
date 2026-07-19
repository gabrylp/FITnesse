package com.fitnesse.app.ui.clothes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnesse.app.data.local.PhotoStorage
import com.fitnesse.app.data.model.ClothingItem
import com.fitnesse.app.data.repository.WardrobeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ClothingDetailUiState(
    val item: ClothingItem? = null,
    val isEditing: Boolean = false,
    val editedCategory: String = "",
    val editedSubcategory: String = "",
    val editedDominantColor: String = "",
    val editedSecondaryColor: String = "",
    val editedPattern: String = "",
    val editedLength: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val deleted: Boolean = false,
    val error: String? = null,
)

class ClothingDetailViewModel(
    private val itemId: String,
    private val repository: WardrobeRepository = WardrobeRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(ClothingDetailUiState())
    val state: StateFlow<ClothingDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val item = repository.getClothingItem(itemId)
            _state.value = _state.value.copy(
                item = item,
                editedCategory = item?.category ?: "",
                editedSubcategory = item?.subcategory ?: "",
                editedDominantColor = item?.dominantColor ?: "",
                editedSecondaryColor = item?.secondaryColor ?: "",
                editedPattern = item?.pattern ?: "",
                editedLength = item?.length ?: "",
                isLoading = false,
            )
        }
    }

    fun toggleEdit() {
        val current = _state.value
        if (current.isEditing) {
            val item = current.item ?: return
            _state.value = current.copy(
                isEditing = false,
                editedCategory = item.category,
                editedSubcategory = item.subcategory,
                editedDominantColor = item.dominantColor,
                editedSecondaryColor = item.secondaryColor,
                editedPattern = item.pattern,
                editedLength = item.length,
            )
        } else {
            _state.value = current.copy(isEditing = true)
        }
    }

    fun updateCategory(v: String) { _state.value = _state.value.copy(editedCategory = v) }
    fun updateSubcategory(v: String) { _state.value = _state.value.copy(editedSubcategory = v) }
    fun updateDominantColor(v: String) { _state.value = _state.value.copy(editedDominantColor = v) }
    fun updateSecondaryColor(v: String) { _state.value = _state.value.copy(editedSecondaryColor = v) }
    fun updatePattern(v: String) { _state.value = _state.value.copy(editedPattern = v) }
    fun updateLength(v: String) { _state.value = _state.value.copy(editedLength = v) }

    fun save() {
        viewModelScope.launch {
            val s = _state.value
            val original = s.item ?: return@launch
            _state.value = s.copy(isSaving = true, error = null)
            try {
                repository.updateClothingItem(
                    original.copy(
                        category = s.editedCategory,
                        subcategory = s.editedSubcategory,
                        dominantColor = s.editedDominantColor,
                        secondaryColor = s.editedSecondaryColor,
                        pattern = s.editedPattern,
                        length = s.editedLength,
                    )
                )
                _state.value = _state.value.copy(isSaving = false, isEditing = false)
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false, error = e.message)
            }
        }
    }

    fun showDeleteConfirm() { _state.value = _state.value.copy(showDeleteConfirm = true) }
    fun hideDeleteConfirm() { _state.value = _state.value.copy(showDeleteConfirm = false) }

    fun delete() {
        viewModelScope.launch {
            try {
                val url = _state.value.item?.photoURL ?: ""
                repository.deleteClothingItem(itemId)
                if (url.startsWith("file://")) PhotoStorage.deletePhoto(url)
                _state.value = _state.value.copy(deleted = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message, showDeleteConfirm = false)
            }
        }
    }
}
