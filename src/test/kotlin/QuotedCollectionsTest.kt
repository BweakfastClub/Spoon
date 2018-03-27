import generator.QuotedArrayList
import generator.QuotedLinkedHashMap
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class QuotedCollectionsTest {
    @Test
    fun testQuotedLinkedHashMap() {
        val map1 = QuotedLinkedHashMap<String, String>(0)
        val map2 = QuotedLinkedHashMap<String, Int>(2).apply {
            put("calories", 12)
            put("fat", 20)
        }

        val mapString1 = "mapOf()"
        val mapString2 = "mapOf(\"calories\" to 12, \"fat\" to 20)"

        assertEquals(mapString1, map1.toString())
        assertEquals(mapString2, map2.toString())
    }

    @Test
    fun testQuotedArrayList() {
        val list1 = QuotedArrayList<String>(0)
        val list2 = QuotedArrayList<Int>(2).apply {
            add(12)
            add(20)
        }
        val list3 = QuotedArrayList<String>(2).apply {
            add("fat")
            add("calories")
        }

        val listString1 = "listOf()"
        val listString2 = "listOf(12, 20)"
        val listString3 = "listOf(\"fat\", \"calories\")"

        assertEquals(listString1, list1.toString())
        assertEquals(listString2, list2.toString())
        assertEquals(listString3, list3.toString())
    }
}