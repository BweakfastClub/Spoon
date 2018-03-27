package models

import com.google.gson.annotations.SerializedName
import quotedStringOrNull

data class NutritionValue(
    val name: String?,
    val amount: Float,
    val unit: String?,
    val displayValue: String?,
    @SerializedName("percentDailyValue") val dailyValue: String?,
    @SerializedName("hasCompleteData") val isCompleteData: Boolean
) {
    override fun toString(): String {
        val name = quotedStringOrNull(this.name)
        val unit = quotedStringOrNull(this.unit)
        val dailyValue = quotedStringOrNull(this.dailyValue)
        val displayValue = quotedStringOrNull(this.displayValue)
        
        return "NutritionValue($name, ${amount}f, $unit, $displayValue, $dailyValue, $isCompleteData)"
    }
}