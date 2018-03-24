package scraper.allrecipes.models.typeadapters

import com.google.gson.stream.JsonReader
import scraper.allrecipes.models.Ingredient
import scraper.allrecipes.models.NutritionValue
import scraper.allrecipes.models.Recipe
import scraper.allrecipes.nextStringOrNull

object Serializers {
    private val dimensionRegex = Regex("\\d+x\\d+/")

    fun serializeRecipe(reader: JsonReader): Recipe = with(reader) {
        val nutrition = mutableMapOf<String, NutritionValue>()
        val ingredients = mutableListOf<Ingredient>()
        
        var id = -1
        var name = ""
        var imageURL = ""
        var servings = -1
        var prepMinutes = -1
        var cookMinutes = -1
        var readyMinutes = -1

        beginObject()
        while (hasNext()) {
            when (nextName()) {
                "title" -> name = nextString()
                "servings" -> servings = nextInt()
                "prepMinutes" -> prepMinutes = nextInt()
                "cookMinutes" -> cookMinutes = nextInt()
                "readyMinutes" -> readyMinutes = nextInt()
                "photo" -> {
                    beginObject()
                    while (hasNext()) {
                        if (nextName() != "urls") {
                            skipValue()
                        } else {
                            beginArray()
                            while (hasNext()) {
                                beginObject()
                                while (hasNext()) {
                                    if (nextName() != "url") {
                                        skipValue()
                                    } else {
                                        imageURL = nextString().replace(dimensionRegex, "")
                                    }
                                }
                                endObject()
                            }
                            endArray()
                        }
                    }
                    endObject()
                }
                "recipeID" -> id = nextInt()
                "nutrition" -> {
                    beginObject()
                    while (hasNext()) {
                        val key = nextName()
                        val nutritionValue = serializeNutritionValue(this)
                        nutrition[key] = nutritionValue
                    }
                    endObject()
                }
                "ingredients" -> {
                    beginArray()
                    while (hasNext()) {
                        ingredients.add(serializeIngredient(reader))
                    }
                    endArray()
                }
                else -> skipValue()
            }
        }


        endObject()

        Recipe(
            ingredients,
            name,
            nutrition,
            servings,
            prepMinutes,
            cookMinutes,
            readyMinutes,
            imageURL
        ).apply { this.id = id }
    }

    fun serializeNutritionValue(reader: JsonReader): NutritionValue = with(reader) {
        var amount = -1f
        var name: String? = null
        var unit: String? = null
        var isCompleteData = false
        var dailyValue: String? = null
        var displayValue: String? = null

        beginObject()
        while (hasNext()) {
            when (nextName()) {
                "name" -> name = nextStringOrNull()
                "unit" -> unit = nextStringOrNull()
                "amount" -> amount = nextDouble().toFloat()
                "dailyValue" -> dailyValue = nextStringOrNull()
                "displayValue" -> displayValue = nextStringOrNull()
                "hasCompleteData" -> isCompleteData = nextBoolean()
                else -> skipValue()
            }
        }
        endObject()

        NutritionValue(name, amount, unit, displayValue, dailyValue, isCompleteData)
    }

    fun serializeIngredient(reader: JsonReader): Ingredient = with(reader) {
        var grams = -1f
        var displayType = ""
        var ingredientID = -1
        var displayValue = ""
        
        beginObject()
        while (hasNext()) {
            when (nextName()) {
                "grams" -> grams = nextDouble().toFloat()
                "displayType" -> displayType = nextString()
                "ingredientID" -> ingredientID = nextInt()
                "displayValue" -> displayValue = nextString()
                else -> skipValue()
            }
        }
        endObject()

        Ingredient(ingredientID, displayValue, grams, displayType)
    }
}