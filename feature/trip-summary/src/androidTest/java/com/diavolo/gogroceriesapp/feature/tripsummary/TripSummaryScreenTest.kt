package com.diavolo.gogroceriesapp.feature.tripsummary

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diavolo.gogroceriesapp.core.ui.LOADING_STATE_TEST_TAG
import com.diavolo.gogroceriesapp.domain.Money
import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.model.GroceryList
import com.diavolo.gogroceriesapp.domain.model.ListStatus
import com.diavolo.gogroceriesapp.domain.model.TripSummary
import com.diavolo.gogroceriesapp.domain.model.UnitOfMeasure
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TripSummaryScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingState_showsSharedProgressState() {
        setScreen(TripSummaryUiState(isLoading = true))

        composeRule.onNodeWithTag(LOADING_STATE_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun summary_showsActualEstimatedAndBudgetAmounts() {
        setScreen(summaryState())

        composeRule.onNodeWithText("Actual spent").assertIsDisplayed()
        composeRule.onNodeWithText("Rp 1.000 above the purchased estimate").assertIsDisplayed()
        composeRule.onNodeWithText("Estimated").assertIsDisplayed()
        composeRule.onNodeWithText("Rp 15.000").assertIsDisplayed()
        composeRule.onNodeWithText("Under budget").assertIsDisplayed()
        composeRule.onNodeWithText("Rp 4.000 remaining").assertIsDisplayed()
    }

    @Test
    fun summary_withMissingPrices_showsWarning() {
        setScreen(summaryState(missingActualPriceCount = 1))

        composeRule.onNodeWithText(
            "1 purchased item has no actual price, so actual spending may be incomplete."
        ).assertIsDisplayed()
    }

    @Test
    fun navigationActions_invokeTheirCallbacks() {
        var backClicked = false
        var doneClicked = false
        setScreen(
            uiState = summaryState(),
            onBackClick = { backClicked = true },
            onDoneClick = { doneClicked = true }
        )

        composeRule.onNodeWithContentDescription("Go back").performClick()
        composeRule.onNodeWithTag("trip-summary-done")
            .performScrollTo()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(backClicked)
            assertTrue(doneClicked)
        }
    }

    private fun setScreen(
        uiState: TripSummaryUiState,
        onBackClick: () -> Unit = {},
        onDoneClick: () -> Unit = {}
    ) {
        composeRule.setContent {
            MaterialTheme {
                TripSummaryScreen(
                    uiState = uiState,
                    onBackClick = onBackClick,
                    onDoneClick = onDoneClick,
                    onRetryClick = {}
                )
            }
        }
    }

    private fun summaryState(missingActualPriceCount: Int = 0): TripSummaryUiState {
        val item = GroceryItem(
            id = 1,
            listId = 1,
            categoryId = null,
            name = "Milk",
            quantity = 1.0,
            unit = UnitOfMeasure.PACK,
            estimatedPriceRupiah = 15_000,
            actualPriceRupiah = 16_000,
            isChecked = true,
            notes = null,
            position = 0
        )
        return TripSummaryUiState(
            isLoading = false,
            list = GroceryList(
                id = 1,
                name = "Weekly groceries",
                status = ListStatus.Completed,
                budgetRupiah = 20_000,
                createdAt = 1,
                updatedAt = 2,
                items = listOf(item)
            ),
            summary = TripSummary(
                actualSpent = Money(16_000),
                estimatedPurchasedTotal = Money(15_000),
                budget = Money(20_000),
                budgetRemainingRupiah = 4_000,
                purchasedItems = listOf(item),
                skippedItems = emptyList(),
                missingActualPriceCount = missingActualPriceCount
            )
        )
    }
}
