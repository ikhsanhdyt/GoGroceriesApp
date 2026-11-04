package com.diavolo.gogroceriesapp.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RupiahPriceParserTest {

    private fun amounts(text: String) = parseRupiahPrices(text).map(PriceCandidate::rupiah)

    @Test
    fun parsesCommonRupiahFormats() {
        assertEquals(listOf(12_500L), amounts("Rp 12.500"))
        assertEquals(listOf(12_500L), amounts("Rp12.500"))
        assertEquals(listOf(12_500L), amounts("RP. 12.500"))
        assertEquals(listOf(12_500L), amounts("rp 12500"))
        assertEquals(listOf(12_500L), amounts("12.500"))
        assertEquals(listOf(12_500L), amounts("12,500"))
        assertEquals(listOf(1_250_000L), amounts("Rp 1.250.000"))
    }

    @Test
    fun dropsDecimalsAndDashSuffix() {
        assertEquals(listOf(12_500L), amounts("Rp 12.500,00"))
        assertEquals(listOf(12_500L), amounts("Rp 12.500,-"))
        assertEquals(listOf(12_500L), amounts("12500.00"))
    }

    @Test
    fun marksAmountsWrittenWithRp() {
        val candidates = parseRupiahPrices("Rp 15.900")

        assertTrue(candidates.single().hasCurrencyPrefix)
        assertEquals(false, parseRupiahPrices("15.900").single().hasCurrencyPrefix)
    }

    @Test
    fun promoTagYieldsBothPricesWithRpFirst() {
        assertEquals(
            listOf(12_500L, 15_900L),
            amounts("Harga normal 15.900  PROMO Rp 12.500")
        )
    }

    @Test
    fun keepsPerUnitPrices() {
        assertEquals(listOf(45_000L), amounts("Rp 45.000/kg"))
    }

    @Test
    fun ignoresSizesAndDiscounts() {
        assertEquals(emptyList<Long>(), amounts("500 g"))
        assertEquals(emptyList<Long>(), amounts("250ml"))
        assertEquals(emptyList<Long>(), amounts("Hemat 10%"))
        assertEquals(listOf(8_900L), amounts("Susu 1 L 1000 ml Rp 8.900"))
    }

    @Test
    fun ignoresBarcodesDatesAndSmallNumbers() {
        assertEquals(emptyList<Long>(), amounts("8992761111111"))
        assertEquals(emptyList<Long>(), amounts("25/09/2026"))
        assertEquals(emptyList<Long>(), amounts("Isi 12"))
    }

    @Test
    fun ignoresNumbersInsideWords() {
        assertEquals(emptyList<Long>(), amounts("SKU A1234"))
    }

    @Test
    fun deduplicatesKeepingTheRpMark() {
        val candidates = parseRupiahPrices("12.500 Rp 12.500")

        assertEquals(listOf(PriceCandidate(12_500, hasCurrencyPrefix = true)), candidates)
    }

    @Test
    fun returnsEmptyForTextWithoutPrices() {
        assertEquals(emptyList<Long>(), amounts("Indomie Goreng"))
        assertEquals(emptyList<Long>(), amounts(""))
    }
}
