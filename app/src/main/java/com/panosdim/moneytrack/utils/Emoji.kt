package com.panosdim.moneytrack.utils

fun extractEmojis(input: String): String {
    val emojiRegex =
        Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]|[\\u2600-\\u27BF]|[\\u0030-\\u0039]\\u20E3|[\\u00A9\\u00AE]\\uFE0F?|\\u3030|\\u303D|\\u200D\\u2764\\uFE0F\\u200D\\u2764\\uFE0F\\u200D\\u2764\\uFE0F|\\u200D\\u2764\\uFE0F\\u200D\\u2764\\uFE0F|\\u200D\\u2764\\uFE0F|\\u200D\\u2620\\uFE0F|\\uD83E\\uDD1D|\\uD83D\\uDC7E")

    val emojis = mutableListOf<String>()
    val matcher = emojiRegex.findAll(input)

    for (match in matcher) {
        emojis.add(match.value)
    }

    return emojis.joinToString(separator = "\u200D")
}

fun removeEmojis(input: String): String {
    val emojiRegex =
        Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]|[\\u2600-\\u27BF]|[\\u0030-\\u0039]\\u20E3|[\\u00A9\\u00AE]\\uFE0F?|\\u3030|\\u303D|\\u200D\\u2764\\uFE0F\\u200D\\u2764\\uFE0F\\u200D\\u2764\\uFE0F|\\u200D\\u2764\\uFE0F\\u200D\\u2764\\uFE0F|\\u200D\\u2764\\uFE0F|\\u200D\\u2620\\uFE0F|\\uD83E\\uDD1D|\\uD83D\\uDC7E")

    return emojiRegex.replace(input, "").trimStart()
}