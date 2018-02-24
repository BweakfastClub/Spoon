package scraper.allrecipes

import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import scraper.allrecipes.models.Recipe
import java.io.File

object RecipeScraper {
    private val gson = Gson()
    private val jsonParser = JsonParser()
    private val sendChannel = Channel<Int>()
    private val defaultRecipe = Recipe(emptyList(), "", emptyMap(), -1, -1, -1, -1)
    private val coroutineContext = newFixedThreadPoolContext(25, "RecipeThread")

    init {
        ReviewsScraper.channel = sendChannel
    }

    fun scrape(category: Pair<Int, String>) = launch {
        try {
            val (categoryID, name) = category
            println("Starting category $categoryID: $name")
            val pageCount = getPageCount(categoryID, 500)
            println("Found last page of category $categoryID to be $pageCount")

            val recipes = (0 until pageCount)
                    .map { i ->
                        val index = i + 1
                        getRecipes(categoryID, index)
                    }
                    .map {
                        try {
                            it.await()
                        } catch (e: Exception) {
                            throw e
                        }
                    }
                    .flatMap { array ->
                        array.map {
                            val recipeID = if (it.asJsonObject.has("associatedRecipeCook")) {
                                it["associatedRecipeCook"]["id"].int
                            } else {
                                it["id"].int
                            }
                            sendChannel.send(recipeID)
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

            val file = File("./data/recipes/${parseName(name)}.json")
            println("Saving recipes for $categoryID")

            file.writeText(gson.toJson(recipes.filter { it != defaultRecipe }))
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
        val (_, _, result) = "/assets/hub-feed?id=$categoryID&pageNumber=$pageNumber&isSponsored=true&sortType=p"
                .httpGet()
                .responseString()
        val (data, error) = result
        if (error == null) {
            val json = jsonParser.parse(data)
            json.asJsonObject["cards"].asJsonArray
        } else {
            throw error
        }
    }

    private fun getRecipe(recipeID: Int) = async(coroutineContext) {
        val (_, _, result) = "/recipes/$recipeID?isMetric=false"
                .httpGet()
                .responseObject<Recipe>()
        val (data, error) = result
        if (error == null) {
            if (data != null) {
                data.id = recipeID
                data
            } else throw NullPointerException("recipe is null")
        } else {
            if (error.response.statusCode == 404) {
                defaultRecipe
            } else {
                throw error
            }
        }
    }

    private fun isLastPage(categoryID: Int, pageNumber: Int) = async(coroutineContext) { getRecipes(categoryID, pageNumber - 1).await().size() > 0 }

    private fun parseName(name: String) = name.toLowerCase().replace(" ", "_").replace("&", "and")
}