package scraper.allrecipes

import khttp.get
import khttp.responses.Response
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
            createFolders()

            val (status, isValid) = hasValidAPIKey()
            if (!isValid) {
                println("Response: $status; Invalid API Key, get a new one")
                System.exit(1)
            }

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

    fun hasValidAPIKey(statusPredicate: (Response) -> Boolean = { it.statusCode in 200 until 300 }): Pair<Int, Boolean> {
        val response = get(
            "https://apps.allrecipes.com/v1/assets/hub-feed?id=659&pageNumber=1&isSponsored=true&sortType=p",
            mapOf("Authorization" to "Bearer $apiKey")
        )
        return response.statusCode to hasValidAPIKey(response, statusPredicate)
    }

    fun hasValidAPIKey(
        response: Response,
        responsePredicate: (Response) -> Boolean = { it.statusCode in 200 until 300 }
    ): Boolean = responsePredicate(response)

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