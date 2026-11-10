package com.diavolo.gogroceriesapp.core.ui

import com.diavolo.gogroceriesapp.core.ui.component.AppTextFieldDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Covers [com.diavolo.gogroceriesapp.core.ui.component.AppTextFieldDecimal], the decimal filter used by [com.diavolo.gogroceriesapp.core.ui.component.AppTextField] via
 * `isDecimal = true` (e.g. quantity fields).
 */
class AppTextFieldDecimalTest {

    @Test
    fun `keeps digits and a single dot`() {
        assertEquals("12", AppTextFieldDecimal.filter("12"))
        assertEquals("1.5", AppTextFieldDecimal.filter("1.5"))
        assertEquals(".5", AppTextFieldDecimal.filter(".5"))
    }

    @Test
    fun `keeps only the first dot`() {
        assertEquals("1.23", AppTextFieldDecimal.filter("1.2.3"))
        assertEquals("1.2", AppTextFieldDecimal.filter("1..2"))
    }

    @Test
    fun `drops letters and signs`() {
        assertEquals("5", AppTextFieldDecimal.filter("-5"))
        assertEquals("", AppTextFieldDecimal.filter("abc"))
    }
}
