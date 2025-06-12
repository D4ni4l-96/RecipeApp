package com.danial.recipeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String,
    val imageUri: String,
    val ingredients: List<String>,
    val steps: List<String>
)