package models

import com.google.gson.annotations.SerializedName

data class Ingredient(
    @SerializedName("ingredientID") val id: Int,
    @SerializedName("displayValue") val name: String,
    val grams: Float,
    val displayType: String
) {
    override fun toString(): String {
        return "Ingredient($id, \"$name\", ${grams}f, \"$displayType\")"
    }
}