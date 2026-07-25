package org.shad.adman.vectora.query

import kotlinx.serialization.json.*
import kotlinx.serialization.serializer

/**
 * On-device, model-free natural language query understanding.
 * Parses a natural language query and populates a generic consumer-defined schema.
 */
class QueryParser(
    @PublishedApi
    internal val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = true
    }
) {

    /**
     * Parses the [query] and populates the [schema].
     * Returns the populated schema of type [T].
     */
    inline fun <reified T> parse(query: String, schema: T): T {
        val schemaJson = json.encodeToString(serializer<T>(), schema)
        val populatedJson = parse(query, schemaJson)
        return json.decodeFromString(serializer<T>(), populatedJson)
    }

    /**
     * Parses the [query] and populates the [schemaJson].
     * Returns the populated schema as a JSON string.
     */
    fun parse(query: String, schemaJson: String): String {
        val schema = json.parseToJsonElement(schemaJson).jsonObject
        val result = parseToElement(query, schema)
        return json.encodeToString(JsonObject.serializer(), result)
    }

    /**
     * Parses the [query] and populates the [schema].
     * Returns the populated schema as a [JsonObject].
     */
    fun parseToElement(query: String, schema: JsonObject): JsonObject {
        val tokens = tokenize(query)
        val extracted = Extractor.extract(tokens)
        return SchemaMapper.map(extracted, schema)
    }

    private fun tokenize(query: String): List<String> {
        return query.lowercase()
            .replace(Regex("[^a-z0-9\\s<>=£$€/\\-]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
    }
}

internal enum class Operator {
    EQ, LT, GT, LTE, GTE, BETWEEN
}

internal data class ExtractedValue(
    val value: Any,
    val operator: Operator = Operator.EQ,
    val secondaryValue: Double? = null,
    val unit: String? = null,
    val originalTokens: List<String> = emptyList()
)

internal object Extractor {
    private val numberRegex = Regex("""\d+(\.\d+)?""")
    private val stopwords = setOf("show", "me", "find", "search", "for", "a", "an", "the", "with", "in", "any", "all", "of", "give", "list", "items", "products", "that", "are", "beside", "near")

    fun extract(tokens: List<String>): List<ExtractedValue> {
        val result = mutableListOf<ExtractedValue>()
        val consumed = mutableSetOf<Int>()

        // 1. Extract Numeric Comparisons and Ranges
        var i = 0
        while (i < tokens.size) {
            if (i in consumed) { i++; continue }

            val opResult = tryExtractNumericOp(tokens, i, consumed)
            if (opResult != null) {
                result.add(opResult.first)
                i = opResult.second
                continue
            }

            val token = tokens[i]
            if (numberRegex.matches(token)) {
                result.add(ExtractedValue(value = token.toDouble(), originalTokens = listOf(token)))
                consumed.add(i)
            }
            i++
        }

        // 2. Extract Basic Date Ranges
        i = 0
        while (i < tokens.size) {
            if (i in consumed) { i++; continue }
            val dateResult = tryExtractDate(tokens, i, consumed)
            if (dateResult != null) {
                result.add(dateResult.first)
                i = dateResult.second
                continue
            }
            i++
        }

        // 3. Extract remaining text values
        for (idx in tokens.indices) {
            if (idx !in consumed) {
                val token = tokens[idx]
                if (token !in stopwords && !numberRegex.matches(token)) {
                    result.add(ExtractedValue(value = token, originalTokens = listOf(token)))
                }
            }
        }

        return result
    }

    private fun tryExtractNumericOp(tokens: List<String>, start: Int, consumed: MutableSet<Int>): Pair<ExtractedValue, Int>? {
        val token = tokens[start]
        
        // "under X", "below X", "less than X"
        if (token == "under" || token == "below") {
            if (start + 1 < tokens.size && numberRegex.matches(tokens[start+1])) {
                consumed.add(start); consumed.add(start + 1)
                return ExtractedValue(value = tokens[start+1].toDouble(), operator = Operator.LT, originalTokens = listOf(token, tokens[start+1])) to start + 2
            }
        }
        if (token == "less" && start + 2 < tokens.size && tokens[start+1] == "than" && numberRegex.matches(tokens[start+2])) {
            consumed.add(start); consumed.add(start+1); consumed.add(start+2)
            return ExtractedValue(value = tokens[start+2].toDouble(), operator = Operator.LT, originalTokens = listOf("less", "than", tokens[start+2])) to start + 3
        }

        // "over X", "above X", "more than X"
        if (token == "over" || token == "above") {
            if (start + 1 < tokens.size && numberRegex.matches(tokens[start+1])) {
                consumed.add(start); consumed.add(start + 1)
                return ExtractedValue(value = tokens[start+1].toDouble(), operator = Operator.GT, originalTokens = listOf(token, tokens[start+1])) to start + 2
            }
        }
        if (token == "more" && start + 2 < tokens.size && tokens[start+1] == "than" && numberRegex.matches(tokens[start+2])) {
            consumed.add(start); consumed.add(start+1); consumed.add(start+2)
            return ExtractedValue(value = tokens[start+2].toDouble(), operator = Operator.GT, originalTokens = listOf("more", "than", tokens[start+2])) to start + 3
        }

        // "between X and Y"
        if (token == "between" && start + 3 < tokens.size && numberRegex.matches(tokens[start+1]) && tokens[start+2] == "and" && numberRegex.matches(tokens[start+3])) {
            consumed.add(start); consumed.add(start+1); consumed.add(start+2); consumed.add(start+3)
            return ExtractedValue(value = tokens[start+1].toDouble(), operator = Operator.BETWEEN, secondaryValue = tokens[start+3].toDouble(), originalTokens = tokens.subList(start, start+4)) to start + 4
        }

        return null
    }

    private fun tryExtractDate(tokens: List<String>, start: Int, consumed: MutableSet<Int>): Pair<ExtractedValue, Int>? {
        val token = tokens[start]
        val dateKeywords = setOf("today", "yesterday", "tomorrow", "now")
        if (token in dateKeywords) {
            consumed.add(start)
            return ExtractedValue(value = token, originalTokens = listOf(token)) to start + 1
        }
        
        // "last 7 days", "past month"
        if ((token == "last" || token == "past") && start + 2 < tokens.size && numberRegex.matches(tokens[start+1])) {
             val next = tokens[start+2]
             if (next in setOf("days", "weeks", "months", "years")) {
                 consumed.add(start); consumed.add(start+1); consumed.add(start+2)
                 return ExtractedValue(value = "$token ${tokens[start+1]} $next", originalTokens = tokens.subList(start, start+3)) to start + 3
             }
        }

        // "last week", "this month", etc.
        if ((token == "last" || token == "this" || token == "next") && start + 1 < tokens.size) {
            val next = tokens[start+1]
            val timeUnits = setOf("week", "month", "year", "day")
            if (next in timeUnits) {
                consumed.add(start); consumed.add(start + 1)
                return ExtractedValue(value = "$token $next", originalTokens = listOf(token, next)) to start + 2
            }
        }
        
        return null
    }
}

internal object SchemaMapper {
    fun map(extracted: List<ExtractedValue>, schema: JsonObject): JsonObject {
        val used = mutableSetOf<ExtractedValue>()
        return mapRecursive(extracted, schema, used)
    }

    private fun mapRecursive(extracted: List<ExtractedValue>, schema: JsonObject, used: MutableSet<ExtractedValue>): JsonObject {
        val result = mutableMapOf<String, JsonElement>()

        // 1. Map Nested Objects first
        schema.forEach { (key, value) ->
            if (value is JsonObject) {
                result[key] = mapRecursive(extracted, value, used)
            }
        }

        // 2. Map primitives
        schema.forEach { (key, value) ->
            if (value !is JsonObject) {
                val matched = findMatchForKey(key, value, extracted, used)
                if (matched != null) {
                    result[key] = matched
                } else if (!result.containsKey(key)) {
                    result[key] = value
                }
            }
        }

        // 3. Handle the main "query" key as a catch-all for remaining text
        if (schema.containsKey("query")) {
            val remainingText = extracted.filter { it !in used && it.value is String && !isDateValue(it) }
                .joinToString(" ") { it.value as String }
            if (remainingText.isNotBlank()) {
                result["query"] = JsonPrimitive(remainingText)
            }
        }

        return JsonObject(result)
    }

    private fun isDateValue(ev: ExtractedValue): Boolean {
        val dateKeywords = setOf("today", "yesterday", "tomorrow", "now", "last", "this", "next", "past")
        return ev.originalTokens.any { it in dateKeywords }
    }

    private fun findMatchForKey(key: String, defaultValue: JsonElement, extracted: List<ExtractedValue>, used: MutableSet<ExtractedValue>): JsonElement? {
        val keyLower = key.lowercase()
        val colorKeywords = setOf(
            "black", "white", "red", "blue", "green", "yellow", "orange", "purple", "pink", "brown", "gray", "grey",
            "cyan", "magenta", "silver", "gold", "navy", "olive", "maroon", "beige", "ivory", "teal", "indigo", "violet"
        )
        
        if (defaultValue is JsonPrimitive) {
            val isNumberField = defaultValue.content.toDoubleOrNull() != null || (defaultValue.isString && defaultValue.content.isEmpty() && (keyLower.contains("price") || keyLower.contains("amount") || keyLower.contains("min") || keyLower.contains("max") || keyLower.contains("count") || keyLower.contains("quantity")))
            
            // Try matching numeric values
            if (isNumberField) {
                // Special handling for BETWEEN
                if (keyLower.contains("min") || keyLower.contains("from")) {
                    val betweenMatch = extracted.find { it !in used && it.operator == Operator.BETWEEN }
                    if (betweenMatch != null) {
                        return JsonPrimitive(betweenMatch.value as Number)
                    }
                }
                if (keyLower.contains("max") || keyLower.contains("to")) {
                    val betweenMatch = extracted.find { it.operator == Operator.BETWEEN }
                    if (betweenMatch != null && betweenMatch.secondaryValue != null) {
                        used.add(betweenMatch)
                        return JsonPrimitive(betweenMatch.secondaryValue)
                    }
                }

                val numericMatch = extracted.filter { it !in used && it.value is Double }.find { ev ->
                    when {
                        keyLower.contains("max") || keyLower.contains("to") || keyLower.contains("less") || keyLower.contains("below") -> ev.operator == Operator.LT || ev.operator == Operator.LTE || ev.operator == Operator.EQ
                        keyLower.contains("min") || keyLower.contains("from") || keyLower.contains("more") || keyLower.contains("above") -> ev.operator == Operator.GT || ev.operator == Operator.GTE || ev.operator == Operator.EQ
                        else -> ev.operator == Operator.EQ
                    }
                }
                if (numericMatch != null) {
                    used.add(numericMatch)
                    return JsonPrimitive(numericMatch.value as Number)
                }
            } else {
                // Try matching text or date
                if (keyLower != "query") {
                    // Dates
                    if (keyLower.contains("date") || keyLower.contains("time") || keyLower.contains("when") || keyLower.contains("created") || keyLower.contains("updated")) {
                        val dateMatch = extracted.find { it !in used && isDateValue(it) }
                        if (dateMatch != null) {
                            used.add(dateMatch)
                            return JsonPrimitive(dateMatch.value.toString())
                        }
                    }

                    // Specialized text matching for common fields
                    if (keyLower.contains("color")) {
                        val colorMatch = extracted.find { it !in used && it.value is String && !isDateValue(it) && colorKeywords.contains(it.value.lowercase()) }
                        if (colorMatch != null) {
                            used.add(colorMatch)
                            return JsonPrimitive(colorMatch.value as String)
                        }
                    }

                    if (keyLower.contains("brand")) {
                        val brandMatch = extracted.find { it !in used && it.value is String && !isDateValue(it) && !colorKeywords.contains(it.value.lowercase()) }
                        if (brandMatch != null) {
                            used.add(brandMatch)
                            return JsonPrimitive(brandMatch.value as String)
                        }
                    }

                    // Generic text
                    val textMatch = extracted.find { it !in used && it.value is String && !isDateValue(it) }
                    if (textMatch != null) {
                        used.add(textMatch)
                        return JsonPrimitive(textMatch.value as String)
                    }
                }
            }
        }
        
        return null
    }
}
