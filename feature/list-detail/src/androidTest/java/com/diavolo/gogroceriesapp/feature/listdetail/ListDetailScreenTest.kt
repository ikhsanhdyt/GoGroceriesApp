package com.diavolo.gogroceriesapp.feature.listdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
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
import com.diavolo.gogroceriesapp.domain.model.UnitOfMeasure
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ListDetailScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingState_showsSharedProgressState() {
        setScreen(ListDetailUiState(isLoading = true))

        composeRule.onNodeWithTag(LOADING_STATE_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun loadError_retryInvokesCallback() {
        var retried = false
        setScreen(
            uiState = ListDetailUiState(
                isLoading = false,
                errorMessage = "Offline"
            ),
            onRetryClick = { retried = true }
        )

        composeRule.onNodeWithText("Try again").performClick()

        composeRule.runOnIdle { assertTrue(retried) }
    }

    @Test
    fun emptyDraft_addFirstItemInvokesCallback() {
        var addClicked = false
        setScreen(
            uiState = detailState(ListStatus.Draft, items = emptyList()),
            onAddItemClick = { addClicked = true }
        )

        composeRule.onNodeWithText("Add first item").performClick()

        composeRule.runOnIdle { assertTrue(addClicked) }
    }

    @Test
    fun completedList_isReadOnlyAndOpensTripSummary() {
        var summaryClicked = false
        setScreen(
            uiState = detailState(ListStatus.Completed, items = listOf(testItem())),
            onTripSummaryClick = { summaryClicked = true }
        )

        composeRule.onNodeWithContentDescription("Actions for Apples").assertDoesNotExist()
        composeRule.onNodeWithText("Add item").assertDoesNotExist()
        composeRule.onNodeWithText("View trip summary")
            .performScrollTo()
            .performClick()

        composeRule.runOnIdle { assertTrue(summaryClicked) }
    }

    private fun setScreen(
        uiState: ListDetailUiState,
        onRetryClick: () -> Unit = {},
        onAddItemClick: () -> Unit = {},
        onTripSummaryClick: () -> Unit = {}
    ) {
        composeRule.setContent {
            MaterialTheme {
                ListDetailScreen(
                    uiState = uiState,
                    onBackClick = {},
                    onRetryClick = onRetryClick,
                    snackbarHostState = remember { SnackbarHostState() },
                    onToggleItem = {},
                    onEditItem = {},
                    onDeleteItem = {},
                    onStartShoppingClick = {},
                    onTripSummaryClick = onTripSummaryClick,
                    onAddItemClick = onAddItemClick
                )
            }
        }
    }

    private fun detailState(
        status: ListStatus,
        items: List<GroceryItem>
    ) = ListDetailUiState(
        isLoading = false,
        list = GroceryList(
            id = 1,
            name = "Weekly groceries",
            status = status,
            budgetRupiah = 100_000,
            createdAt = 1,
            updatedAt = 2,
            items = items
        ),
        itemGroups = if (items.isEmpty()) {
            emptyList()
        } else {
            listOf(GroceryItemGroup("Other", items))
        },
        estimatedTotal = Money(15_000)
    )

    private fun testItem() = GroceryItem(
        id = 1,
        listId = 1,
        categoryId = null,
        name = "Apples",
        quantity = 1.0,
        unit = UnitOfMeasure.PACK,
        estimatedPriceRupiah = 15_000,
        actualPriceRupiah = 14_000,
        isChecked = true,
        notes = null,
        position = 0
    )
}
