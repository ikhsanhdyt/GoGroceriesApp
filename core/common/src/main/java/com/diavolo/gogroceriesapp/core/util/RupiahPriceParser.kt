package com.diavolo.gogroceriesapp.core.util

/**
 * A rupiah amount found in free text, e.g. from OCR of a price tag.
 *
 * [hasCurrencyPrefix] is true when the amount was written with "Rp", which makes it far more
 * likely to be the price than a stray number on the label.
 */
data class PriceCandidate(
    val rupiah: Long,
    val hasCurrencyPrefix: Boolean
)

private const val MIN_PRICE_RUPIAH = 100L
private const val MAX_PRICE_RUPIAH = 99_999_999L

// Plain digit runs this long are barcodes or product codes, not prices.
private const val MIN_CODE_DIGITS = 8

private val PRICE_PATTERN = Regex(
    """(?<![\p{L}\d.,/])""" +
        """(rp\.?\s*)?""" +
        // Either grouped thousands (12.500 / 12,500 / 1.250.000) or a plain digit run.
        """(\d{1,3}(?:[.,]\d{3})+|\d+)""" +
        // Optional decimals ("12.500,00") or a dash ("12.500,-"); both are dropped.
        """(?:[.,]\d{2}(?!\d)|\s*[.,]-)?""" +
        """(?!\d)""" +
        // A trailing unit means a size or discount, not a price.
        """(\s*(?:%|kg|gr|g|ml|l|pcs|pc|cm|mm)(?!\p{L}))?""",
    RegexOption.IGNORE_CASE
)

/**
 * Extracts plausible rupiah prices from [text], most likely first: amounts written with "Rp"
 * come before bare numbers, otherwise in reading order. Each amount appears once.
 *
 * Sizes and discounts ("500 g", "1 L", "10%"), barcodes, and amounts outside
 * Rp 100 – Rp 99.999.999 are ignored.
 */
fun parseRupiahPrices(text: String): List<PriceCandidate> {
    val candidates = PRICE_PATTERN.findAll(text).mapNotNull { match ->
        val hasPrefix = match.groups[1] != null
        val digits = match.groupValues[2]
        val hasUnit = match.groups[3] != null
        when {
            hasUnit && !hasPrefix -> null
            !hasPrefix && digits.all(Char::isDigit) && digits.length >= MIN_CODE_DIGITS -> null
            else -> digits.filter(Char::isDigit).toLongOrNull()
                ?.takeIf { it in MIN_PRICE_RUPIAH..MAX_PRICE_RUPIAH }
                ?.let { PriceCandidate(rupiah = it, hasCurrencyPrefix = hasPrefix) }
        }
    }.toList()

    return candidates
        .groupBy(PriceCandidate::rupiah)
        .map { (rupiah, sameAmount) ->
            PriceCandidate(rupiah, hasCurrencyPrefix = sameAmount.any { it.hasCurrencyPrefix })
        }
        .sortedByDescending(PriceCandidate::hasCurrencyPrefix)
}
