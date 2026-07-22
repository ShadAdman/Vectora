package org.shad.adman.vectora.engine.preprocessor

/**
 * A basic BERT WordPiece tokenizer implementation.
 */
class WordPieceTokenizer(private val vocab: List<String>) {
    private val vocabMap = vocab.withIndex().associate { it.value to it.index }
    private val unkToken = "[UNK]"
    private val maskToken = "[MASK]"
    private val clsToken = "[CLS]"
    private val sepToken = "[SEP]"
    private val padToken = "[PAD]"

    fun tokenize(text: String): List<String> {
        val outputTokens = mutableListOf<String>()
        val words = text.split(Regex("\\s+")).filter { it.isNotEmpty() }

        for (word in words) {
            val chars = word.toCharArray()
            var start = 0
            var isBad = false
            val subTokens = mutableListOf<String>()

            while (start < chars.size) {
                var end = chars.size
                var curSubstr: String? = null
                while (start < end) {
                    var substr = chars.concatToString(start, end)
                    if (start > 0) {
                        substr = "##$substr"
                    }
                    if (vocabMap.containsKey(substr)) {
                        curSubstr = substr
                        break
                    }
                    end--
                }
                if (curSubstr == null) {
                    isBad = true
                    break
                }
                subTokens.add(curSubstr)
                start = end
            }

            if (isBad) {
                outputTokens.add(unkToken)
            } else {
                outputTokens.addAll(subTokens)
            }
        }
        return outputTokens
    }

    fun convertTokensToIds(tokens: List<String>): IntArray {
        return tokens.map { vocabMap[it] ?: vocabMap[unkToken]!! }.toIntArray()
    }

    fun getClsId(): Int = vocabMap[clsToken] ?: 101
    fun getSepId(): Int = vocabMap[sepToken] ?: 102
    fun getPadId(): Int = vocabMap[padToken] ?: 0
}
