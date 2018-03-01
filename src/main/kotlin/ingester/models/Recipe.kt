package ingester.models

import com.google.gson.JsonObject

data class Recipe(
    val ingredients: List<Ingredient>,
    val title: String,
    val nutrition: Map<String, JsonObject>,
    val servings: Int
)