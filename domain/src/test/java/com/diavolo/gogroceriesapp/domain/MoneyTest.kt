package com.diavolo.gogroceriesapp.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyTest {

    @Test
    fun `formats zero and small amounts`() {
        assertEquals("Rp 0", Money.zero().toString())
        assertEquals("Rp 500", Money(500).toString())
    }

    @Test
    fun `groups thousands with dots`() {
        assertEquals("Rp 1.000", Money(1_000).toString())
        assertEquals("Rp 1.500", Money(1_500).toString())
        assertEquals("Rp 100.000", Money(100_000).toString())
        assertEquals("Rp 1.234.567", Money(1_234_567).toString())
    }

    @Test
    fun `puts the minus sign before the currency`() {
        assertEquals("-Rp 1.500", Money(-1_500).toString())
        assertEquals("-Rp 12.345", Money(-12_345).toString())
    }

    @Test
    fun `negative amounts with a multiple of three digits keep a clean grouping`() {
        assertEquals("-Rp 500", Money(-500).toString())
        assertEquals("-Rp 100.000", Money(-100_000).toString())
        assertEquals("-Rp 1.234.567", Money(-1_234_567).toString())
    }

    @Test
    fun `formatting the smallest long does not throw`() {
        assertEquals("-Rp 9.223.372.036.854.775.808", Money(Long.MIN_VALUE).toString())
    }

    @Test
    fun `plus adds the rupiah amounts`() {
        assertEquals(Money(1_500), Money(1_000) + Money(500))
    }

    @Test
    fun `fromRupiah rounds to the nearest whole rupiah`() {
        assertEquals("Rp 1.500", Money.fromRupiah(1_499.5).toString())
        assertEquals("Rp 1.499", Money.fromRupiah(1_499.4).toString())
    }
}
