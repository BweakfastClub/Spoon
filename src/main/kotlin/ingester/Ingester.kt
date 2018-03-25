package ingester

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.mongodb.ConnectionString
import com.mongodb.async.client.MongoClientSettings
import com.mongodb.async.client.MongoClients
import com.mongodb.async.client.MongoDatabase
import com.mongodb.connection.ClusterSettings
import ingester.models.Recipe
import kotlinx.coroutines.experimental.launch
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

object Ingester {
    private val gson = Gson()

    fun run() = launch {
        val pojoCodecRegistry = CodecRegistries.fromRegistries(
            MongoClients.getDefaultCodecRegistry(),
            CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        )
        val clusterSettings = ClusterSettings
            .builder()
            .applyConnectionString(ConnectionString("mongodb://localhost"))
            .build()

        MongoClients.create(
            MongoClientSettings
                .builder()
                .codecRegistry(pojoCodecRegistry)
                .clusterSettings(clusterSettings)
                .build()
        )
            .use { client ->
                val db = client.getDatabase("development")
                println(clearDB(db))

                File("./data/recipes")
                    .walkBottomUp()
                    .filter { it.isFile }
                    .map { saveJSONInDB(db, it) }
                    .forEach { it.join() }
            }
    }

    private fun saveJSONInDB(database: MongoDatabase, file: File) = launch {
        val recipes = gson.fromJson<List<Recipe>>(JsonReader(InputStreamReader(FileInputStream(file))))
        val documents = recipes.map {
            with(it) {
                Document(
                    mapOf(
                        "ingredients" to ingredients,
                        "title" to title,
                        "nutrition" to nutrition,
                        "servings" to servings,
                        "prepMinutes" to prepMinutes,
                        "cookMinutes" to cookMinutes,
                        "readyMinutes" to readyMinutes
                    )
                )
            }
        }.toMutableList()

        database.getCollection("recipes").insertMany(documents) { _, error ->
            if (error != null) {
                throw error
            } else {
                println("Successfully ingested ${recipes.size} recipes")
            }
        }
    }
}