package main

import generator.RecipeCreator
import ingester.Ingester
import kotlinx.coroutines.experimental.runBlocking
import scraper.allrecipes.Scraper

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.contains("-h")) {
            showHelp()
            return
        }

        val arguments = listOf(
            Argument("--scrape", "scraper", Scraper::run),
            Argument("--ingest", "ingester", Ingester::run),
            Argument("--generate", "generator", RecipeCreator::run)
        )

        if (args.isNotEmpty()) {
            arguments.forEach { (argument, tag, function) ->
                runBlocking {
                    if (args.contains(argument)) {
                        val job = function.invoke()
                        if (tag.isNotEmpty()) println("Running $tag")
                        job.join()
                    }
                }
            }
        }
    }

    private fun showHelp() {
        println("usage: gradlew[.bat] run -PappArgs=\"[-h] [--scrape] [--ingest]\"")
        println("optional arguments:")
        println("-h\t\t\tShow the help screen")
        println("--scrape\tScrape the recipe data from AllRecipes")
        println("--ingest\tIngest the scraped data and save it into Cassandra")
    }
}