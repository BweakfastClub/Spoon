package scraper.allrecipes

import com.github.salomonbrys.kotson.registerTypeAdapter
import com.google.gson.GsonBuilder
import scraper.allrecipes.models.Ingredient
import scraper.allrecipes.models.NutritionValue
import scraper.allrecipes.models.Recipe
import scraper.allrecipes.models.typeadapters.Deserializers
import scraper.allrecipes.models.typeadapters.Serializers
import java.util.concurrent.TimeUnit

internal const val delayTime = 200L
internal val delayUnit = TimeUnit.MILLISECONDS
internal const val apiKey = "84XAr4Z2Z57+ieH6qwuIrfKGbxdQ/WiVs0WHQVuDuew42+/KsDBSkuoyzzxZIMNGsNc+Gpp2fHUHEn57L9OLO8sepkQl/NNgt9Wtpq7V4OvdX1Nr+A4JGCdywpkYmuenIvXJG18isHh/ecKG+c1Y85gpbUWtJGTQm5pRgu1dn+Hi5oTIKNFrcA=="
internal val gson = GsonBuilder()
    .registerTypeAdapter<Recipe> {
        write { Deserializers.deserializeRecipe(this, it) }
        read { Serializers.serializeRecipe(this) }
    }
    .registerTypeAdapter<Ingredient> {
        write { Deserializers.deserializeIngredient(this, it) }
        read { Serializers.serializeIngredient(this) }
    }
    .registerTypeAdapter<NutritionValue> {
        write { Deserializers.deserializeNutritionValue(this, it) }
        read { Serializers.serializeNutritionValue(this) }
    }
    .setPrettyPrinting()
    .create()