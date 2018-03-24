package scraper.allrecipes.models.typeadapters

import com.google.gson.stream.JsonWriter
import scraper.allrecipes.models.Ingredient
import scraper.allrecipes.models.NutritionValue
import scraper.allrecipes.models.Recipe

object Deserializers {
    fun deserializeRecipe(writer: JsonWriter, value: Recipe) {
        with(writer) {
            beginObject()
            name("title")
            value(value.name)
            name("nutrition")
            beginObject()
            value.nutrition.forEach { key, nutritionValue ->
                name(key)
                Deserializers.deserializeNutritionValue(writer, nutritionValue)
            }
            endObject()
            name("ingredients")
            beginArray()
            value.ingredients.forEach { Deserializers.deserializeIngredient(writer, it) }
            endArray()
            name("servings")
            value(value.servings)
            name("prepMinutes")
            value(value.prepMinutes)
            name("cookMinutes")
            value(value.cookMinutes)
            name("readyMinutes")
            value(value.readyMinutes)
            name("imageUrl")
            value(value.imageURL)
            name("id")
            value(value.id)
            endObject()
        }
    }

    fun deserializeNutritionValue(writer: JsonWriter, value: NutritionValue) {
        with(writer) {
            beginObject()
            name("name")
            value(value.name)
            name("amount")
            value(value.amount)
            name("unit")
            value(value.unit)
            name("displayValue")
            value(value.displayValue)
            name("dailyValue")
            value(value.displayValue)
            name("isCompleteData")
            value(value.isCompleteData)
            endObject()
        }
    }

    fun deserializeIngredient(writer: JsonWriter, value: Ingredient) {
        with(writer) {
            beginObject()
            name("ingredientID")
            value(value.ingredientID)
            name("displayValue")
            value(value.displayValue)
            name("grams")
            value(value.grams)
            name("displayType")
            value(value.displayValue)
            endObject()
        }
    }
}