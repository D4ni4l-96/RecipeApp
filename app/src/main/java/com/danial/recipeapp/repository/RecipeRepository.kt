package com.danial.recipeapp.repository

import com.danial.recipeapp.data.Recipe
import com.danial.recipeapp.data.RecipeDao

class RecipeRepository(private val recipeDao: RecipeDao) {

    suspend fun getAllRecipes(): List<Recipe> = recipeDao.getAll()

    suspend fun getRecipesByType(type: String): List<Recipe> = recipeDao.getByType(type)
}
