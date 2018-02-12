package scraper.allrecipes

import com.github.kittinunf.fuel.core.FuelManager
import kotlinx.coroutines.experimental.runBlocking
import kotlin.coroutines.experimental.suspendCoroutine

object Test {
    @JvmStatic
    fun main(args: Array<String>) {
        FuelManager.instance.basePath = "https://apps.allrecipes.com/v1"
//        FuelManager.instance.basePath = "http://httpbin.org"
        FuelManager.instance.baseHeaders = mapOf("Authorization" to "Bearer $apiKey")
        runBlocking {
            println(test() + test())
        }
    }

    suspend fun test() = suspendCoroutine<String> {
        println("yes?")
        Thread.sleep(2000)
        it.resume("hi")
    }


}