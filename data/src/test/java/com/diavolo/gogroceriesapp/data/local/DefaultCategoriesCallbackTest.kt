package com.diavolo.gogroceriesapp.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultCategoriesCallbackTest {

    private val db = mockk<SupportSQLiteDatabase>()

    @Test
    fun `onCreate inserts every default category in aisle order`() {
        val insertedArgs = mutableListOf<Array<Any?>>()
        every { db.execSQL(any(), capture(insertedArgs)) } returns Unit

        DefaultCategoriesCallback().onCreate(db)

        assertEquals(
            DEFAULT_CATEGORIES.map { listOf(it.name, it.colorHex, it.aisleOrder) },
            insertedArgs.map { it.toList() }
        )
    }

    @Test
    fun `onOpen does not insert anything`() {
        DefaultCategoriesCallback().onOpen(db)

        verify(exactly = 0) { db.execSQL(any(), any()) }
    }
}
