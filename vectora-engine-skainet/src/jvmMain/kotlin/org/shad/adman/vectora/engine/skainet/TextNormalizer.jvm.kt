package org.shad.adman.vectora.engine.skainet

import java.text.Normalizer

internal actual fun stripAccents(text: String): String {
    val decomposed = Normalizer.normalize(text, Normalizer.Form.NFD)
    return buildString(decomposed.length) {
        for (ch in decomposed) {
            if (Character.getType(ch) != Character.NON_SPACING_MARK.toInt()) append(ch)
        }
    }
}
