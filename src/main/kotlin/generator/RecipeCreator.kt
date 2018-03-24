package generator

import com.github.salomonbrys.kotson.float
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.string
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import ingester.models.Recipe
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

object RecipeCreator {
    fun run() = launch {
        val gson = Gson()
        val recipesFile = File("Recipes.kt")
        recipesFile.delete()

        mapOf(
            "breakfastRecipes" to "appetizer.json",
            "lunchRecipes" to "quick_and_easy.json",
            "dinnerRecipes" to "main_dish.json",
            "snacks" to "dessert.json"
        )
            .mapValues { (_, value) -> File("data/recipes/$value") }
            .forEach { varName, file ->
                val recipes = gson.fromJson<List<Recipe>>(JsonReader(InputStreamReader(FileInputStream(file)))).take(10)
                recipesFile.appendText(
                    """val $varName = ${recipes.map {
                        with(it) {
                            """Recipe(
                        |$ingredients,
                        |"$title",
                        |${nutrition.toList().associate {
                                val unit = if (it.second.has("unit")) {
                                    it.second["unit"].string
                                } else {
                                    ""
                                }
                                it.first to "${it.second["amount"].float}$unit"
                            }},
                        |$servings,
                        |"$imageURL"
                        |)""".trimMargin()
                        }
                    }}
                )
                """.replace("[", "listOf(").replace("]", ")")
                )
            }
    }
}