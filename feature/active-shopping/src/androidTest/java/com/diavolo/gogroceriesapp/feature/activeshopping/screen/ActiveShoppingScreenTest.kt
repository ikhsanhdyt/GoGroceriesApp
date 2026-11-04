package com.diavolo.gogroceriesapp.feature.activeshopping.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diavolo.gogroceriesapp.core.entity.common.Money
import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryItem
import com.diavolo.gogroceriesapp.core.entity.grocery.GroceryList
import com.diavolo.gogroceriesapp.core.entity.grocery.ListStatus
import com.diavolo.gogroceriesapp.core.entity.grocery.UnitOfMeasure
import com.diavolo.gogroceriesapp.core.ui.LOADING_STATE_TEST_TAG
import com.diavolo.gogroceriesapp.feature.activeshopping.screen.attr.ActiveShoppingAttr.TAG_FINISH_SHOPPING
import com.diavolo.gogroceriesapp.feature.activeshopping.screen.attr.ActiveShoppingAttr.tagSetPrice
import com.diavolo.gogroceriesapp.feature.activeshopping.screen.attr.ActiveShoppingAttr.tagShoppingItem
import com.diavolo.gogroceriesapp.feature.activeshopping.state.ActiveShoppingUiState
import com.diavolo.gogroceriesapp.feature.activeshopping.state.ShoppingItemGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActiveShoppingScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingState_showsSharedProgressState() {
        setScreen(ActiveShoppingUiState(isLoading = true))

        composeRule.onNodeWithTag(LOADING_STATE_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun activeItem_toggleInvokesCallback() {
        var toggledItem: GroceryItem? = null
        setScreen(
            uiState = shoppingState(),
            onToggleItem = { toggledItem = it }
        )

        composeRule.onNodeWithTag(tagShoppingItem(1)).performClick()

        composeRule.runOnIdle { assertEquals(testItem(), toggledItem) }
    }

    @Test
    fun setPriceInvokesCallback() {
        var pricedItem: GroceryItem? = null
        setScreen(
            uiState = shoppingState(),
            onPriceClick = { pricedItem = it }
        )

        composeRule.onNodeWithTag(tagSetPrice(1)).performClick()

        composeRule.runOnIdle { assertEquals(testItem(), pricedItem) }
    }

    @Test
    fun finishShoppingInvokesCallback() {
        var finishClicked = false
        setScreen(
            uiState = shoppingState(),
            onFinishClick = { finishClicked = true }
        )

        composeRule.onNodeWithTag(TAG_FINISH_SHOPPING).performClick()

        composeRule.runOnIdle { assertTrue(finishClicked) }
    }

    @Test
    fun completedList_showsReadOnlyState() {
        setScreen(shoppingState(status = ListStatus.Completed))

        composeRule.onNodeWithText("Shopping completed").assertIsDisplayed()
        composeRule.onNodeWithTag(TAG_FINISH_SHOPPING).assertDoesNotExist()
        composeRule.onNodeWithTag(tagShoppingItem(1)).assertDoesNotExist()
    }

    private fun setScreen(
        uiState: ActiveShoppingUiState,
        onToggleItem: (GroceryItem) -> Unit = {},
        onPriceClick: (GroceryItem) -> Unit = {},
        onFinishClick: () -> Unit = {}
    ) {
        composeRule.setContent {
            MaterialTheme {
                ActiveShoppingScreen(
                    uiState = uiState,
                    snackbarHostState = remember { SnackbarHostState() },
                    onBackClick = {},
                    onRetryClick = {},
                    onToggleItem = onToggleItem,
                    onPriceClick = onPriceClick,
                    onFinishClick = onFinishClick
                )
            }
        }
    }

    private fun shoppingState(status: ListStatus = ListStatus.Active): ActiveShoppingUiState {
        val item = testItem()
        return ActiveShoppingUiState(
            isLoading = false,
            list = GroceryList(
                id = 1,
                name = "Weekly groceries",
                status = status,
                budgetRupiah = 100_000,
                createdAt = 1,
                updatedAt = 2,
                items = listOf(item)
            ),
            itemGroups = listOf(ShoppingItemGroup("Other", listOf(item))),
            estimatedTotal = Money(12_000),
            actualCheckedSubtotal = Money.zero()
        )
    }

    private fun testItem() = GroceryItem(
        id = 1,
        listId = 1,
        categoryId = null,
        name = "Milk",
        quantity = 1.0,
        unit = UnitOfMeasure.PACK,
        estimatedPriceRupiah = 12_000,
        actualPriceRupiah = null,
        isChecked = false,
        notes = null,
        position = 0
    )
}
