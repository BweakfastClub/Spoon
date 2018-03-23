package scraper.allrecipes.models

data class Ingredient(
    val ingredientID: Int,
    val displayValue: String,
    val grams: Float,
    val displayType: String
)