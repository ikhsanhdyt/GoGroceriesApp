package com.diavolo.gogroceriesapp.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Renders the real composable, so it covers what the unit tests cannot: that `isCurrency` is
 * actually wired into the rendered field, and that the [AppTextField] refactor for it did not
 * break plain (non-currency) fields.
 */
class AppTextFieldTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun currencyField_showsThousandSeparatorsWhileKeepingRawDigits() {
        var raw = ""
        composeRule.setContent {
            var value by remember { mutableStateOf("") }
            MaterialTheme {
                AppTextField(
                    value = value,
                    onValueChange = {
                        value = it
                        raw = it

                    },
                    label = "Budget",
                    isCurrency = true,
                    prefix = "Rp "
                )
            }
        }

        composeRule.onNode(hasSetTextAction()).performTextInput("1500000")

        composeRule.onNodeWithText("1.500.000").assertIsDisplayed()
        assertEquals("1500000", raw)
    }

    @Test
    fun currencyField_dropsNonDigitCharacters() {
        var raw = ""
        composeRule.setContent {
            var value by remember { mutableStateOf("") }
            MaterialTheme {
                AppTextField(
                    value = value,
                    onValueChange = {
                        value = it
                        raw = it
                    },
                    label = "Budget",
                    isCurrency = true
                )
            }
        }

        composeRule.onNode(hasSetTextAction()).performTextInput("Rp 50.000abc")

        assertEquals("50000", raw)
        composeRule.onNodeWithText("50.000").assertIsDisplayed()
    }

    @Test
    fun currencyField_formatsAnExistingValue() {
        composeRule.setContent {
            MaterialTheme {
                AppTextField(
                    value = "1234567",
                    onValueChange = {},
                    label = "Estimated price",
                    isCurrency = true,
                    prefix = "Rp "
                )
            }
        }

        composeRule.onNodeWithText("1.234.567").assertIsDisplayed()
    }

    @Test
    fun decimalField_keepsDigitsAndASingleDot() {
        var raw = ""
        composeRule.setContent {
            var value by remember { mutableStateOf("") }
            MaterialTheme {
                AppTextField(
                    value = value,
                    onValueChange = {
                        value = it
                        raw = it
                    },
                    label = "Quantity",
                    isDecimal = true
                )
            }
        }

        composeRule.onNode(hasSetTextAction()).performTextInput("1.2.3abc")

        assertEquals("1.23", raw)
        composeRule.onNodeWithText("1.23").assertIsDisplayed()
    }

    @Test
    fun plainField_withMaxLength_stillTruncates() {
        var raw = ""
        composeRule.setContent {
            var value by remember { mutableStateOf("") }
            MaterialTheme {
                AppTextField(
                    value = value,
                    onValueChange = {
                        value = it
                        raw = it
                    },
                    label = "Note",
                    maxLength = 5
                )
            }
        }

        composeRule.onNode(hasSetTextAction()).performTextInput("Weekly groceries")

        assertEquals("Weekl", raw)
        composeRule.onNodeWithText("Weekl").assertIsDisplayed()
    }
}
