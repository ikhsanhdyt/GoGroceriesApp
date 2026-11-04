package com.diavolo.gogroceriesapp.core.navigation

import com.diavolo.gogroceriesapp.core.navigation.navargs.activeshopping.ActiveShoppingNavArgs
import com.diavolo.gogroceriesapp.core.navigation.navargs.analytics.AnalyticsNavArgs
import com.diavolo.gogroceriesapp.core.navigation.navargs.categories.CategoriesNavArgs
import com.diavolo.gogroceriesapp.core.navigation.navargs.home.HomeNavArgs
import com.diavolo.gogroceriesapp.core.navigation.navargs.listdetail.ListDetailNavArgs
import com.diavolo.gogroceriesapp.core.navigation.navargs.tripsummary.TripSummaryNavArgs
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Type-safe navigation serializes each route at runtime; these tests fail if a route stops being
 * serializable or loses its argument.
 */
class NavArgsTest {

    @Test
    fun `routes with an argument round trip through serialization`() {
        assertEquals(
            ListDetailNavArgs(7),
            roundTrip(ListDetailNavArgs(7), ListDetailNavArgs.serializer())
        )
        assertEquals(
            ActiveShoppingNavArgs(7),
            roundTrip(ActiveShoppingNavArgs(7), ActiveShoppingNavArgs.serializer())
        )
        assertEquals(
            TripSummaryNavArgs(7),
            roundTrip(TripSummaryNavArgs(7), TripSummaryNavArgs.serializer())
        )
    }

    @Test
    fun `routes without an argument round trip through serialization`() {
        assertEquals(HomeNavArgs, roundTrip(HomeNavArgs, HomeNavArgs.serializer()))
        assertEquals(AnalyticsNavArgs, roundTrip(AnalyticsNavArgs, AnalyticsNavArgs.serializer()))
        assertEquals(
            CategoriesNavArgs,
            roundTrip(CategoriesNavArgs, CategoriesNavArgs.serializer())
        )
    }

    @Test
    fun `the list id is the only argument`() {
        assertEquals(
            """{"listId":42}""",
            Json.encodeToString(ListDetailNavArgs.serializer(), ListDetailNavArgs(42))
        )
    }

    @Test
    fun `routes with the same list id are different destinations`() {
        assertNotEquals(ListDetailNavArgs(1) as Any, ActiveShoppingNavArgs(1) as Any)
        assertNotEquals(ActiveShoppingNavArgs(1) as Any, TripSummaryNavArgs(1) as Any)
    }

    private fun <T> roundTrip(value: T, serializer: KSerializer<T>): T =
        Json.decodeFromString(serializer, Json.encodeToString(serializer, value))
}
