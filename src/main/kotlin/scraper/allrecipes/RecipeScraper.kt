package scraper.allrecipes

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.google.gson.JsonParser
import khttp.get
import khttp.responses.Response
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import scraper.allrecipes.Scraper.hasValidAPIKey
import scraper.allrecipes.models.Recipe
import java.io.File

object RecipeScraper {
    private val jsonParser = JsonParser()
    private val reviewChannel = Channel<Int>()
    private val defaultRecipe = Recipe(emptyList(), "", emptyMap(), -1, -1, -1, -1, "")
    private val coroutineContext = newFixedThreadPoolContext(25, "RecipeThread")

    fun scrape(category: Pair<Int, String>) = launch {
        val (categoryID, name) = category

        try {
            println("Starting category $categoryID: $name")
            val pageCount = getPageCount(categoryID, 500)
            println("Found last page of category $categoryID to be $pageCount")

            val recipes = (0 until pageCount)
                .map { i ->
                    val index = i + 1
                    getRecipes(categoryID, index)
                }
                .flatMap { array ->
                    array.await().map {
                        val recipeID = if (it.asJsonObject.has("associatedRecipeCook")) {
                            it["associatedRecipeCook"]["id"].int
                        } else {
                            it["id"].int
                        }
//                        ReviewsScraper.scrape(recipeID, 100)
                        getRecipe(recipeID)
                    }
                }
                .map {
                    try {
                        it.await()
                    } catch (e: Exception) {
                        throw e
                    }
                }
                .filter { it != defaultRecipe }
                .distinct()

            val file = File("./data/recipes/${parseName(name)}.json")
            println("Saving recipes for $categoryID")

            file.bufferedWriter().use { gson.toJson(recipes, it) }
            println("Finished category $categoryID: $name")
            println("Saved to ${file.name}")
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun getPageCount(categoryID: Int, maxIncrement: Int): Int {
        var pageNumber = 1
        var increment = maxIncrement
        do {
            val recipes = getRecipes(categoryID, pageNumber).await()
            when {
                recipes.size() > 0 -> pageNumber += increment
                isLastPage(categoryID, pageNumber).await() -> return pageNumber
                else -> {
                    pageNumber -= increment
                    increment = Math.floorDiv(increment, 2)
                    pageNumber += increment
                }
            }
        } while (true)
    }

    private fun getRecipes(categoryID: Int, pageNumber: Int) = async(coroutineContext) {
        var response: Response
        var validResponse: Boolean
        do {
            response = get(
                "https://apps.allrecipes.com/v1/assets/hub-feed?id=$categoryID&pageNumber=$pageNumber&isSponsored=true&sortType=p",
                mapOf("Authorization" to "Bearer $apiKey")
            )

            val isValid = hasValidAPIKey(response) { it.statusCode != 401 }
            if (!isValid) {
                println("Invalid API Key, get a new one")
                System.exit(1)
            }

            validResponse = response.statusCode == 200
            if (!validResponse) {
                println("Delaying category $categoryID page $pageNumber for $delayTime ${delayUnit.toString().toLowerCase()}")
                delay(delayTime, delayUnit)
            }
        } while (!validResponse)

        val json = jsonParser.parse(response.text)
        json.asJsonObject["cards"].asJsonArray
    }

    private fun getRecipe(recipeID: Int) = async(coroutineContext) {
        var response: Response
        var validResponse: Boolean

        do {
            response = get(
                "https://apps.allrecipes.com/v1/recipes/$recipeID?isMetric=false",
                mapOf("Authorization" to "Bearer $apiKey")
            )

            val isValid = hasValidAPIKey(response) { it.statusCode != 401 }
            if (!isValid) {
                println("Invalid API Key, get a new one")
                System.exit(1)
            }

            validResponse = response.statusCode == 200
            if (!validResponse) {
                println("Delaying recipe $recipeID for $delayTime ${delayUnit.toString().toLowerCase()}")
                delay(delayTime, delayUnit)
            }
        } while (!validResponse)
//        println("Recipe ID: $recipeID; Status code: ${response.statusCode}; JSON: ${response.text}")
        if (response.statusCode == 404) {
            defaultRecipe
        } else {
            val data = gson.fromJson<Recipe>(response.text)
            data.id = recipeID
            data
        }
    }

    private fun isLastPage(categoryID: Int, pageNumber: Int) =
        async(coroutineContext) { getRecipes(categoryID, pageNumber - 1).await().size() > 0 }

    private fun parseName(name: String) = name.toLowerCase().replace(" ", "_").replace("&", "and")
}