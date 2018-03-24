package scraper.allrecipes

import com.google.gson.stream.JsonReader

fun JsonReader.nextStringOrNull(): String? {
    try {
        return nextString()
    } catch (e: IllegalStateException) {
        if (e.message?.contains("Expected a string but was NULL")!!) {
            nextNull()
            return null
        }
        throw e
    }
}