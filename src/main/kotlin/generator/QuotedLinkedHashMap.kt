package generator

private const val INT_MAX_POWER_OF_TWO: Int = Int.MAX_VALUE / 2 + 1

class QuotedLinkedHashMap<K, V>(initialCapacity: Int) : LinkedHashMap<K, V>(initialCapacity) {
    override fun toString(): String {
        return "mapOf(${this.entries.joinToString(", ") { (k, v) ->
            var key = k.toString()
            var value = v.toString()

            if (k is String) key = key.replace("\"", "\\\"")
            if (v is String) value = value.replace("\"", "\\\"")
            
            if (k is String && v is String) "\"$key\" to \"$value\""
            else if (k is String && v !is String) "\"$key\" to $value"
            else if (k !is String && v is String) "$key to \"$value\""
            else "$key to $value"
        }})"
    }
}

inline fun <T, K, V> List<T>.associate(transform: (T) -> Pair<K, V>): QuotedLinkedHashMap<K, V> {
    val capacity = mapCapacity(collectionSizeOrDefault(10)).coerceAtLeast(16)
    return associateTo(QuotedLinkedHashMap(capacity), transform)
}

fun mapCapacity(expectedSize: Int): Int {
    if (expectedSize < 3) {
        return expectedSize + 1
    }
    if (expectedSize < INT_MAX_POWER_OF_TWO) {
        return expectedSize + expectedSize / 3
    }
    return Int.MAX_VALUE // any large value
}

fun <T> Iterable<T>.collectionSizeOrDefault(default: Int): Int = if (this is Collection<*>) this.size else default

