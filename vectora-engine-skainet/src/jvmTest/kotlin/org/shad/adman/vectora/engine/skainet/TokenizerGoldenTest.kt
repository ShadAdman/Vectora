package org.shad.adman.vectora.engine.skainet

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import sk.ainet.models.bert.HuggingFaceTokenizer
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@Serializable
private data class GoldenCase(val text: String, val input_ids: List<Int>)

/**
 * Golden parity against HuggingFace `tokenizers` output for
 * sentence-transformers/all-MiniLM-L6-v2 (fixtures: gen_goldens.py).
 * Cases prefixed TRUNC256: were generated with truncation at max_length=256.
 */
class TokenizerGoldenTest {

    private fun resource(name: String): String =
        checkNotNull(javaClass.classLoader.getResourceAsStream(name)) { "missing test resource $name" }
            .readBytes().decodeToString()

    private val tokenizer = HuggingFaceTokenizer.fromVocabTxt(resource("vocab.txt"))
    private val goldens = Json.decodeFromString<List<GoldenCase>>(resource("tokenizer_goldens.json"))

    @Test
    fun vocabIsComplete() {
        assertEquals(30522, tokenizer.vocabSize)
    }

    @Test
    fun matchesHuggingFaceGoldens() {
        for (case in goldens) {
            val (text, maxLen) = if (case.text.startsWith("TRUNC256:")) {
                case.text.removePrefix("TRUNC256:") to 256
            } else {
                case.text to Int.MAX_VALUE
            }
            val output = tokenizer.encodeForBert(text, maxLen)
            assertContentEquals(
                case.input_ids.toIntArray(),
                output.inputIds,
                "token ids diverge for: ${text.take(60)}"
            )
            assertEquals(output.inputIds.size, output.attentionMask.size)
            assertContentEquals(IntArray(output.inputIds.size) { 1 }, output.attentionMask)
        }
    }
}
