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
 * Renders the real composable, so it covers what the unit tests cannot: that the preset's filter
 * and visual transformation are actually wired into the text field.
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
                    input = AppTextFieldInput.Currency,
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
                    input = AppTextFieldInput.Currency
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
                    input = AppTextFieldInput.Currency,
                    prefix = "Rp "
                )
            }
        }

        composeRule.onNodeWithText("1.234.567").assertIsDisplayed()
    }

    @Test
    fun textField_leavesTheInputUntouched() {
        composeRule.setContent {
            var value by remember { mutableStateOf("") }
            MaterialTheme {
                AppTextField(value = value, onValueChange = { value = it }, label = "List name")
            }
        }

        composeRule.onNode(hasSetTextAction()).performTextInput("Weekly 1500")

        composeRule.onNodeWithText("Weekly 1500").assertIsDisplayed()
    }
}
