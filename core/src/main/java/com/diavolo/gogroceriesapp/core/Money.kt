package com.diavolo.gogroceriesapp.core

@JvmInline
value class Money(val amount: Long) {
    val toDouble: Double
        get() = amount.toDouble()

    override fun toString(): String {
        val formatted = amount.toString()
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()
        return "Rp $formatted"
    }

    companion object {
        fun fromRupiah(rupiah: Double): Money {
            return Money(rupiah.toLong())
        }

        fun zero(): Money = Money(0)
    }
}

fun Long.toMoney(): Money = Money(this)

fun Double.toMoney(): Money = Money.fromRupiah(this)