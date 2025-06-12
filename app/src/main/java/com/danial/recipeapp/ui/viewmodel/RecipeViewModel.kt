package com.danial.recipeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danial.recipeapp.data.Recipe
import com.danial.recipeapp.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _filteredRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val filteredRecipes: StateFlow<List<Recipe>> = _filteredRecipes.asStateFlow()

    private var currentFilter: String? = null

    init {
        refreshRecipes()
    }

    fun setFilter(type: String?) {
        currentFilter = type
        refreshRecipes()
    }

    fun refreshRecipes() {
        viewModelScope.launch {
            _filteredRecipes.value = if (currentFilter.isNullOrEmpty()) {
                repository.getAllRecipes()
            } else {
                repository.getRecipesByType(currentFilter!!)
            }
        }
    }
}
