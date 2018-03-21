package scraper.allrecipes.models

import com.google.gson.JsonArray
import com.google.gson.annotations.SerializedName

data class Recipe(
        val ingredients: List<Ingredient>,
        @SerializedName("title") val name: String,
        val nutrition: Map<String, NutritionValue>,
        val servings: Int,
        val prepMinutes: Int,
        val cookMinutes: Int,
        val readyMinutes: Int,
        @SerializedName("imageUrl") val imageURL: String
) {
    var id = -1
    var reviews = emptyList<Int>()
    var ratings = emptyMap<String, Int>()
}