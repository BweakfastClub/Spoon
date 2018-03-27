package generator

class QuotedArrayList<T>(initialCapacity: Int) : ArrayList<T>(initialCapacity) {
    override fun toString(): String {
        return "listOf(${joinToString(", ") {
            if (it is String) {
                "\"$it\""
            } else {
                it.toString()
            }
        }})"
    }
}