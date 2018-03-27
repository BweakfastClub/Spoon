package models

import com.google.gson.annotations.SerializedName
import generator.associate

data class Recipe(
    val ingredients: List<Ingredient>,
    val title: String,
    val nutrition: Map<String, NutritionValue>,
    val servings: Int,
    val prepMinutes: Int,
    val cookMinutes: Int,
    val readyMinutes: Int,
    @SerializedName("imageUrl") val imageURL: String
) {
    var id = -1

    /**
     *
     */
    override fun toString(): String {
        return """Recipe(
                        |$ingredients,
                        |"$title",
                        |$servings,
                        |$prepMinutes,
                        |$cookMinutes,
                        |$readyMinutes,
                        |"$imageURL"
                        |).apply {
                        |nutrition = ${nutrition.toList().associate { it.first to it.second }}
                        |}""".trimMargin()
    }
}