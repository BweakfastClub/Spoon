package ingester

import com.datastax.driver.core.BatchStatement
import com.datastax.driver.core.Cluster
import com.datastax.driver.core.PreparedStatement
import com.datastax.driver.core.Session
import com.datastax.driver.core.utils.UUIDs
import com.github.salomonbrys.kotson.float
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.string
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import ingester.models.Recipe
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

object Ingester {
    private val gson = Gson()

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        Cluster
            .builder()
            .addContactPoint("127.0.0.1")
            .build().use { cluster ->
            val session = cluster.connect("development")
            val statement =
                session.prepare("INSERT INTO recipes (ingredients, title, nutrition, servings, id) VALUES (?, ?, ?, ?, ?)")

            File("./data/recipes")
                .walkBottomUp()
                .filter { it.isFile }
                .map { saveJSONInCassandra(session, statement, it) }
                .forEach { it.join() }
        }
    }

    private fun saveJSONInCassandra(session: Session, statement: PreparedStatement, file: File) = launch {
        val batchSize = 50
        val recipes = gson.fromJson<List<Recipe>>(JsonReader(InputStreamReader(FileInputStream(file))))
        val statements = recipes.map {
            statement.bind(
                it.ingredients.associate { it.id to it.name },
                it.title,
                it.nutrition.toList().associate {
                    val unit = if (it.second.has("unit")) {
                        it.second["unit"].string
                    } else {
                        ""
                    }
                    it.first to "${it.second["amount"].float}$unit"
                },
                it.servings,
                UUIDs.timeBased()
            )
        }.toMutableList()

        while (statements.isNotEmpty()) {
            val removedElements = statements.take(batchSize)
            session.execute(
                BatchStatement().addAll(removedElements)
            )
            statements.removeAll(removedElements)
            println("${statements.size} statements left - ${file.absolutePath}")
        }
    }
}