package com.diavolo.gogroceriesapp.core.ui

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Covers [AppTextFieldCurrency], the currency filter/formatter used by [AppTextField] via
 * `isCurrency = true`.
 */
class AppTextFieldCurrencyTest {

    @Test
    fun `filter keeps only digits`() {
        assertEquals("50000", AppTextFieldCurrency.filter("Rp 50.000"))
        assertEquals("", AppTextFieldCurrency.filter("abc"))
        assertEquals("", AppTextFieldCurrency.filter(""))
    }

    @Test
    fun `filter caps the length so toLongOrNull cannot overflow`() {
        val filtered = AppTextFieldCurrency.filter("1234567890123456")

        assertEquals("123456789012345", filtered)
        assertEquals(15, filtered.length)
        assertEquals(123_456_789_012_345L, filtered.toLongOrNull())
    }

    @Test
    fun `shows dots as thousand separators`() {
        assertEquals("", display(""))
        assertEquals("1", display("1"))
        assertEquals("123", display("123"))
        assertEquals("1.234", display("1234"))
        assertEquals("100.000", display("100000"))
        assertEquals("1.234.567", display("1234567"))
    }

    @Test
    fun `cursor offsets survive a round trip for every length`() {
        for (length in 0..15) {
            val digits = "1234567890123456".take(length)
            val mapping = transform(digits).offsetMapping

            for (offset in 0..length) {
                assertEquals(
                    "length=$length offset=$offset",
                    offset,
                    mapping.transformedToOriginal(mapping.originalToTransformed(offset))
                )
            }
        }
    }

    private fun transform(digits: String) =
        AppTextFieldCurrency.visualTransformation.filter(AnnotatedString(digits))

    private fun display(digits: String): String = transform(digits).text.text
}
