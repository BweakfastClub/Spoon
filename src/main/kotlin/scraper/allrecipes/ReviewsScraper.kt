package scraper.allrecipes

import com.github.salomonbrys.kotson.double
import com.google.gson.JsonParser
import khttp.get
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import java.io.File

object ReviewsScraper {
    private val jsonParser = JsonParser()
    private val coroutineContext = newFixedThreadPoolContext(15, "ReviewsThread")

    fun scrape(recipeID: Int, pageSize: Int = 10) = launch(coroutineContext) {
        val pageCount = getPageCount(recipeID, pageSize).await()

        val reviews = (0 until pageCount)
            .map { getReviews(recipeID, it, pageSize) }
            .map { it.await() }
            .flatten()

        val file = File("./data/reviews/reviews_$recipeID.json")
        file.bufferedWriter().use { gson.toJson(reviews, it) }
        println("Saved to ${file.name}")
    }

    private fun getPageCount(recipeID: Int, pageSize: Int) = async(coroutineContext) {
        val response = get(
            "https://apps.allrecipes.com/v1/recipes/$recipeID/reviews/?pagesize=1&sorttype=HelpfulCountDescending",
            mapOf("Authorization" to "Bearer $apiKey")
        )
        val json = jsonParser.parse(response.text)
        Math.ceil(json.asJsonObject["metaData"].asJsonObject["totalCount"].double / pageSize).toInt()
    }

    private fun getReviews(recipeID: Int, pageNumber: Int, pageSize: Int) = async(coroutineContext) {
        //        println("Scraping reviews for $recipeID")
        val response = get(
            "https://apps.allrecipes.com/v1/recipes/$recipeID/reviews/?page=$pageNumber&pagesize=$pageSize&sorttype=HelpfulCountDescending",
            mapOf("Authorization" to "Bearer $apiKey")
        )
        val json = jsonParser.parse(response.text)
        json.asJsonObject["reviews"].asJsonArray
    }
}