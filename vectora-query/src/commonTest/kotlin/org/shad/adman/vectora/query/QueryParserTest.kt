package org.shad.adman.vectora.query

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Serializable
data class SearchSchema(
    val query: String = "",
    val filters: Filters = Filters()
)

@Serializable
data class Filters(
    val brand: String = "",
    val color: String = "",
    val maxPrice: Double = 0.0
)

@Serializable
data class FinanceFilters(
    val category: String = "",
    val minAmount: Double = 0.0,
    val maxAmount: Double = 0.0,
    val date: String = ""
)

@Serializable
data class FinanceSearchSchema(
    val query: String = "",
    val filters: FinanceFilters = FinanceFilters()
)

@Serializable
data class TravelSchema(
    val destination: String = "",
    val maxBudget: Double = 0.0,
    val whenDate: String = ""
)

class QueryParserTest {

    private val parser = QueryParser()

    @Test
    fun testGenericExtraction() {
        val schema = SearchSchema()
        val query = "Show me black Nike shoes under 100"
        
        val result = parser.parse(query, schema)

        assertEquals("shoes", result.query)
        assertEquals("nike", result.filters.brand)
        assertEquals("black", result.filters.color)
        assertEquals(100.0, result.filters.maxPrice)
    }

    @Test
    fun testBasicExtraction() {
        val schema = """
        {
          "query": "",
          "filters": {
            "brand": "",
            "color": "",
            "maxPrice": 0
          }
        }
        """.trimIndent()

        val query = "Show me black Nike shoes under 100"
        val resultJson = parser.parse(query, schema)
        val result = Json.parseToJsonElement(resultJson).jsonObject

        assertEquals("shoes", result["query"]?.jsonPrimitive?.content)
        
        val filters = result["filters"]?.jsonObject
        assertTrue(filters != null)
        
        assertEquals("nike", filters["brand"]?.jsonPrimitive?.content)
        assertEquals("black", filters["color"]?.jsonPrimitive?.content)
        assertEquals(100.0, filters["maxPrice"]?.jsonPrimitive?.double)
    }

    @Test
    fun testFinanceExtraction() {
        val schema = FinanceSearchSchema()
        val query = "Find food expenses over 50 from yesterday"
        val result = parser.parse(query, schema)

        assertEquals("expenses", result.query)
        assertEquals("food", result.filters.category)
        assertEquals(50.0, result.filters.minAmount)
        assertEquals("yesterday", result.filters.date)
    }

    @Test
    fun testTravelExtraction() {
        val schema = TravelSchema()
        val query = "trip to Paris under 2000 next month"
        val result = parser.parse(query, schema)

        assertEquals("paris", result.destination)
        assertEquals(2000.0, result.maxBudget)
        assertEquals("next month", result.whenDate)
    }

    @Test
    fun testNumericRanges() {
        val schema = """
        {
          "minPrice": 0,
          "maxPrice": 0
        }
        """.trimIndent()

        val query = "between 50 and 150"
        val resultJson = parser.parse(query, schema)
        val result = Json.parseToJsonElement(resultJson).jsonObject

        assertEquals(50.0, result["minPrice"]?.jsonPrimitive?.double)
        assertEquals(150.0, result["maxPrice"]?.jsonPrimitive?.double)
    }

    @Test
    fun testDates() {
        val schema = """
        {
          "date": ""
        }
        """.trimIndent()

        val query = "Show items from last week"
        val resultJson = parser.parse(query, schema)
        var result = Json.parseToJsonElement(resultJson).jsonObject
        assertEquals("last week", result["date"]?.jsonPrimitive?.content)

        val query2 = "items in the last 7 days"
        val resultJson2 = parser.parse(query2, schema)
        result = Json.parseToJsonElement(resultJson2).jsonObject
        assertEquals("last 7 days", result["date"]?.jsonPrimitive?.content)
    }
}
