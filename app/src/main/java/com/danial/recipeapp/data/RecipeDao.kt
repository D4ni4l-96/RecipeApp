package com.danial.recipeapp.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete


@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes")
    suspend fun getAll(): List<Recipe>

    @Query("SELECT * FROM recipes WHERE type = :type")
    suspend fun getByType(type: String): List<Recipe>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getById(id: Int): Recipe?

    @Insert
    suspend fun insert(recipe: Recipe): Long

    @Update
    suspend fun update(recipe: Recipe)

    @Delete
    suspend fun delete(recipe: Recipe)
}
