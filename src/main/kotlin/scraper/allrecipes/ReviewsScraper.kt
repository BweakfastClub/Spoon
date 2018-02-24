package scraper.allrecipes

import com.github.kittinunf.fuel.httpGet
import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.int
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import java.io.File

object ReviewsScraper {
    private val gson = Gson()
    private val jsonParser = JsonParser()
    private val coroutineContext = newFixedThreadPoolContext(25, "ReviewsThread")
    var channel: ReceiveChannel<Int> = Channel(0)
        set(value) {
            field = value
            launch {
                for (recipeID in channel) {
                    scrape(recipeID)
                }
            }
        }

    private fun scrape(recipeID: Int, pageSize: Int = 10) = async(coroutineContext) {
        val pageCount = getPageCount(recipeID, pageSize).await()
        val reviewIDs = mutableListOf<Int>()
        val ratings = (5 downTo 1).map {
            "$it-star" to 0
        }
                .associate { it }
                .toMutableMap()

        val reviews = (0..pageCount)
                .map {
                    getReviews(recipeID, it, pageSize)
                }
                .map { it.await() }
                .reduce { acc, jsonArray ->
                    acc.addAll(jsonArray)
                    acc
                }

        reviews.forEach {
            ratings["${it.asJsonObject["rating"]}-star"] = ratings["${it.asJsonObject["rating"]}-star"]!! + 1
            reviewIDs.add(it.asJsonObject["reviewID"].int)
        }

        val file = File("./data/reviews/reviews_$recipeID.json")
        file.writeText(gson.toJson(reviews))
        println("Saved to ${file.name}")

        ratings to reviewIDs
    }

    private fun getPageCount(recipeID: Int, pageSize: Int) = async(coroutineContext) {
        val (_, _, result) = "/recipes/$recipeID/reviews/?pagesize=$pageSize&sorttype=HelpfulCountDescending"
                .httpGet()
                .responseString()
        val (data, error) = result
        if (error == null) {
            val json = jsonParser.parse(data)
            Math.ceil(json.asJsonObject["metaData"].asJsonObject["totalCount"].double / pageSize).toInt()
        } else {
            throw error
        }
    }

    private fun getReviews(recipeID: Int, pageNumber: Int, pageSize: Int) = async(coroutineContext) {
        val (_, _, result) = "/recipes/$recipeID/reviews/?page=$pageNumber&pagesize=$pageSize&sorttype=HelpfulCountDescending"
                .httpGet()
                .responseString()
        val (data, error) = result
        if (error == null) {
            val json = jsonParser.parse(data)
            json.asJsonObject["reviews"].asJsonArray
        } else {
            throw error
        }
    }
}