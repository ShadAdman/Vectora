package org.shad.adman.vectora.engine.skainet

/**
 * Unicode NFD normalization + removal of combining marks (category Mn),
 * matching HuggingFace BasicTokenizer's strip_accents behavior.
 */
internal expect fun stripAccents(text: String): String

/**
 * Mirrors HuggingFace BasicTokenizer preprocessing for uncased BERT models:
 * control-character cleanup, accent stripping, and spacing around CJK
 * ideographs so they tokenize per character. SK-TR's HuggingFaceTokenizer
 * handles lowercasing and WordPiece itself but not these normalization steps.
 */
internal fun normalizeForBert(text: String): String {
    val cleaned = buildString(text.length) {
        for (ch in text) {
            val code = ch.code
            if (code == 0 || code == 0xFFFD || (ch.isISOControl() && ch != '\t' && ch != '\n' && ch != '\r')) continue
            append(if (ch.isWhitespace()) ' ' else ch)
        }
    }
    return spaceOutCjk(stripAccents(cleaned))
}

private fun spaceOutCjk(text: String): String {
    val sb = StringBuilder(text.length + 16)
    var i = 0
    while (i < text.length) {
        val ch = text[i]
        val codePoint = if (ch.isHighSurrogate() && i + 1 < text.length && text[i + 1].isLowSurrogate()) {
            val cp = ((ch.code - 0xD800) shl 10) + (text[i + 1].code - 0xDC00) + 0x10000
            i++
            cp
        } else {
            ch.code
        }
        if (isCjkIdeograph(codePoint)) {
            sb.append(' ')
            appendCodePoint(sb, codePoint)
            sb.append(' ')
        } else {
            appendCodePoint(sb, codePoint)
        }
        i++
    }
    return sb.toString()
}

private fun appendCodePoint(sb: StringBuilder, codePoint: Int) {
    if (codePoint <= 0xFFFF) {
        sb.append(codePoint.toChar())
    } else {
        val cp = codePoint - 0x10000
        sb.append(((cp shr 10) + 0xD800).toChar())
        sb.append(((cp and 0x3FF) + 0xDC00).toChar())
    }
}

/** HuggingFace BasicTokenizer's _is_chinese_char ranges (CJK ideographs only — kana is not split). */
private fun isCjkIdeograph(cp: Int): Boolean =
    cp in 0x4E00..0x9FFF ||
        cp in 0x3400..0x4DBF ||
        cp in 0x20000..0x2A6DF ||
        cp in 0x2A700..0x2B73F ||
        cp in 0x2B740..0x2B81F ||
        cp in 0x2B820..0x2CEAF ||
        cp in 0xF900..0xFAFF ||
        cp in 0x2F800..0x2FA1F
