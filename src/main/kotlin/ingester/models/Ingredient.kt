package ingester.models

import com.google.gson.annotations.SerializedName

data class Ingredient(
    @SerializedName("ingredientID") val id: Int,
    @SerializedName("displayValue") val name: String,
    val grams: Float,
    val displayType: String
)