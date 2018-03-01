package scraper.allrecipes

import com.github.kittinunf.fuel.core.FuelManager
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.io.File

object Scraper {
    val categories = listOf(
            76 to "Appetizer",
            78 to "Breakfast & Brunch",
            201 to "Chicken",
            79 to "Dessert",
            84 to "Healthy",
            85 to "Holidays and Events",
            17235 to "Magazine Favourites",
            80 to "Main Dish",
            1947 to "Quick & Easy",
            253 to "Slow Cooker",
            87 to "Vegetarian"
    )

    fun run() = launch {
        try {
            FuelManager.instance.basePath = "https://apps.allrecipes.com/v1"
            FuelManager.instance.baseHeaders = mapOf("Authorization" to "Bearer $apiKey")

            createFolders()

            categories
                    .map {
                        RecipeScraper.scrape(it)
                    }
                    .forEach {
                        runBlocking {
                            it.join()
                        }
                    }
        } catch (e: Exception) {
            throw e
        }
    }

    private fun createFolders() {
        createFolder("./data/recipes")
        createFolder("./data/reviews")
    }

    private fun createFolder(path: String) {
        val directory = File(path)
        if (!directory.exists()) {
            directory.mkdirs()
        }
    }
}