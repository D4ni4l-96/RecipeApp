package com.danial.recipeapp.util

import android.content.Context
import org.json.JSONArray

fun loadRecipeTypesFromJson(context: Context): List<String> {
    val jsonString = context.assets.open("recipetypes.json")
        .bufferedReader().use { it.readText() }
    val typeList = JSONArray(jsonString)
    val result = mutableListOf<String>()
    for (i in 0 until typeList.length()) {
        result.add(typeList.getJSONObject(i).getString("name"))
    }
    return result
}
