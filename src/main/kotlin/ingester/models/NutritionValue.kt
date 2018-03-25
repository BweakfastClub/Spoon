package ingester.models

import com.google.gson.annotations.SerializedName

data class NutritionValue(
        val name: String,
        val amount: Float,
        val unit: String,
        val displayValue: String,
        @SerializedName("percentDailyValue") val dailyValue: String,
        @SerializedName("hasCompleteData") val isCompleteData: Boolean
)