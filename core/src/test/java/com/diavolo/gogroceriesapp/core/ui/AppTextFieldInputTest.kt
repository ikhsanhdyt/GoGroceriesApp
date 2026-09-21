package com.diavolo.gogroceriesapp.core.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppTextFieldInputTest {

    // region Currency filter

    @Test
    fun `currency filter keeps only digits`() {
        val filter = AppTextFieldInput.Currency.inputFilter

        assertEquals("50000", filter("Rp 50.000"))
        assertEquals("", filter("abc"))
        assertEquals("", filter(""))
    }

    @Test
    fun `currency filter caps the length so toLongOrNull cannot overflow`() {
        val filter = AppTextFieldInput.Currency.inputFilter

        val filtered = filter("1234567890123456")

        assertEquals("123456789012345", filtered)
        assertEquals(15, filtered.length)
        assertEquals(123_456_789_012_345L, filtered.toLongOrNull())
    }

    // endregion

    // region Decimal filter

    @Test
    fun `decimal filter keeps digits and a single dot`() {
        val filter = AppTextFieldInput.Decimal.inputFilter

        assertEquals("12", filter("12"))
        assertEquals("1.5", filter("1.5"))
        assertEquals(".5", filter(".5"))
    }

    @Test
    fun `decimal filter keeps only the first dot`() {
        val filter = AppTextFieldInput.Decimal.inputFilter

        assertEquals("1.23", filter("1.2.3"))
        assertEquals("1.2", filter("1..2"))
    }

    @Test
    fun `decimal filter drops letters and signs`() {
        val filter = AppTextFieldInput.Decimal.inputFilter

        assertEquals("5", filter("-5"))
        assertEquals("", filter("abc"))
    }

    // endregion

    @Test
    fun `text preset leaves the input unchanged`() {
        assertEquals("Weekly groceries 1!", AppTextFieldInput.Text.inputFilter("Weekly groceries 1!"))
    }

    @Test
    fun `presets pick the matching keyboard`() {
        val text = AppTextFieldInput.Text.keyboardOptions
        val currency = AppTextFieldInput.Currency.keyboardOptions
        val decimal = AppTextFieldInput.Decimal.keyboardOptions

        // Unspecified means the platform's default text keyboard.
        assertTrue(text.keyboardType == KeyboardType.Unspecified || text.keyboardType == KeyboardType.Text)
        assertEquals(ImeAction.Next, text.imeAction)
        assertEquals(KeyboardType.Number, currency.keyboardType)
        assertEquals(ImeAction.Done, currency.imeAction)
        assertEquals(KeyboardType.Decimal, decimal.keyboardType)
        assertEquals(ImeAction.Next, decimal.imeAction)
    }

    // region Thousand separator display

    @Test
    fun `currency shows dots as thousand separators`() {
        assertEquals("", display(""))
        assertEquals("1", display("1"))
        assertEquals("123", display("123"))
        assertEquals("1.234", display("1234"))
        assertEquals("100.000", display("100000"))
        assertEquals("1.234.567", display("1234567"))
        assertEquals("123.456.789.012.345", display("123456789012345"))
    }

    @Test
    fun `cursor offsets map from digits to the formatted text`() {
        val mapping = transform("1234567").offsetMapping

        // Formatted text is "1.234.567".
        assertEquals(
            listOf(0, 1, 3, 4, 5, 7, 8, 9),
            (0..7).map(mapping::originalToTransformed)
        )
    }

    @Test
    fun `cursor offsets map from the formatted text back to digits`() {
        val mapping = transform("1234567").offsetMapping

        assertEquals(
            listOf(0, 1, 1, 2, 3, 4, 4, 5, 6, 7),
            (0..9).map(mapping::transformedToOriginal)
        )
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

    @Test
    fun `out of range cursor offsets are clamped`() {
        val mapping: OffsetMapping = transform("1234").offsetMapping

        assertEquals(5, mapping.originalToTransformed(99))
        assertEquals(0, mapping.originalToTransformed(-3))
        assertEquals(4, mapping.transformedToOriginal(99))
        assertEquals(0, mapping.transformedToOriginal(-3))
    }

    // endregion

    private fun transform(digits: String) =
        AppTextFieldInput.Currency.visualTransformation.filter(AnnotatedString(digits))

    private fun display(digits: String): String = transform(digits).text.text
}
