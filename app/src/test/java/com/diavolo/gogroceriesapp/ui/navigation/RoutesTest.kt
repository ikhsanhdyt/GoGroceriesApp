package com.diavolo.gogroceriesapp.ui.navigation

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Type-safe navigation serializes each route at runtime; these tests fail if a route stops being
 * serializable or loses its argument.
 */
class RoutesTest {

    @Test
    fun `routes with an argument round trip through serialization`() {
        assertEquals(ListDetail(7), roundTrip(ListDetail(7), ListDetail.serializer()))
        assertEquals(ActiveShopping(7), roundTrip(ActiveShopping(7), ActiveShopping.serializer()))
        assertEquals(TripSummary(7), roundTrip(TripSummary(7), TripSummary.serializer()))
    }

    @Test
    fun `routes without an argument round trip through serialization`() {
        assertEquals(Home, roundTrip(Home, Home.serializer()))
        assertEquals(Analytics, roundTrip(Analytics, Analytics.serializer()))
        assertEquals(Categories, roundTrip(Categories, Categories.serializer()))
    }

    @Test
    fun `the list id is the only argument`() {
        assertEquals("""{"listId":42}""", Json.encodeToString(ListDetail.serializer(), ListDetail(42)))
    }

    @Test
    fun `routes with the same list id are different destinations`() {
        assertNotEquals(ListDetail(1) as Any, ActiveShopping(1) as Any)
        assertNotEquals(ActiveShopping(1) as Any, TripSummary(1) as Any)
    }

    private fun <T> roundTrip(
        value: T,
        serializer: kotlinx.serialization.KSerializer<T>
    ): T = Json.decodeFromString(serializer, Json.encodeToString(serializer, value))
}
