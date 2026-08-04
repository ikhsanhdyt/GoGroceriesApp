package com.diavolo.gogroceriesapp.domain

import kotlin.math.roundToLong

@JvmInline
value class Money(val rupiah: Long) {
    operator fun plus(other: Money): Money = Money(rupiah + other.rupiah)

    val toDouble: Double
        get() = rupiah.toDouble()

    override fun toString(): String {
        val formatted = rupiah.toString()
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()
        return "Rp $formatted"
    }

    companion object {
        fun fromRupiah(rupiah: Double): Money {
            return Money(rupiah.roundToLong())
        }

        fun zero(): Money = Money(0)
    }
}

fun Long.toMoney(): Money = Money(this)

fun Double.toMoney(): Money = Money.fromRupiah(this)
