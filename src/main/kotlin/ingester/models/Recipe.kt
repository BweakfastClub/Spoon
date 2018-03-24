package ingester.models

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class Recipe(
    val ingredients: List<Ingredient>,
    val title: String,
    val nutrition: Map<String, JsonObject>,
    val servings: Int,
    @SerializedName("imageUrl") val imageURL: String
)