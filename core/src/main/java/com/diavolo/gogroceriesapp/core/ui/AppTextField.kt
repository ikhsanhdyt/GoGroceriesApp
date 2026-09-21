package com.diavolo.gogroceriesapp.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Reusable input behavior for [AppTextField]: keyboard, filtering and display formatting.
 * Explicit parameters on [AppTextField] take precedence over the values of a preset.
 */
@Immutable
data class AppTextFieldInput(
    val keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    val inputFilter: (String) -> String = { it },
    val visualTransformation: VisualTransformation = VisualTransformation.None
) {
    companion object {
        /** Free text. */
        val Text = AppTextFieldInput(
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        /**
         * Whole-rupiah amount: number keyboard, digits only, shown with "." thousand separators.
         * The value stays the raw digit string, so `toLongOrNull()` works as before.
         */
        val Currency = AppTextFieldInput(
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            // Capped below Long.MAX_VALUE's 19 digits so toLongOrNull() can't overflow.
            inputFilter = { it.filter(Char::isDigit).take(MAX_CURRENCY_DIGITS) },
            visualTransformation = ThousandSeparatorTransformation
        )

        /** Decimal number (e.g. quantity): decimal keyboard, digits and a single '.'. */
        val Decimal = AppTextFieldInput(
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            inputFilter = String::toDecimalInput
        )
    }
}

/**
 * Outlined text field with a notched label, optional prefix / suffix / trailing icon and
 * supporting text.
 *
 * Use [input] for a preset ([AppTextFieldInput.Currency], [AppTextFieldInput.Decimal], ...) and
 * [keyboardOptions], [inputFilter] or [visualTransformation] to override any part of it.
 *
 * @param labelBackground must match the color behind the field (e.g. the sheet or dialog container)
 * so the label appears to cut through the border.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    input: AppTextFieldInput = AppTextFieldInput.Text,
    placeholder: String? = null,
    prefix: String? = null,
    suffix: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = input.keyboardOptions,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    inputFilter: (String) -> String = input.inputFilter,
    visualTransformation: VisualTransformation = input.visualTransformation,
    labelBackground: Color = MaterialTheme.colorScheme.surface,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val borderColor = when {
        isError -> colorScheme.error
        !enabled -> colorScheme.onSurface.copy(alpha = 0.38f)
        else -> colorScheme.primary
    }
    val textColor = if (enabled) colorScheme.onSurface else colorScheme.onSurface.copy(alpha = 0.38f)
    val shape = RoundedCornerShape(4.dp)

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp)
                .clip(shape)
                .border(2.dp, borderColor, shape)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                prefix?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = { onValueChange(inputFilter(it)) },
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    readOnly = readOnly,
                    singleLine = singleLine,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    visualTransformation = visualTransformation,
                    cursorBrush = SolidColor(colorScheme.primary),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty() && placeholder != null) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                )
                suffix?.let {
                    Text(
                        text = it,
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor
                    )
                }
                trailingIcon?.invoke()
            }
            Text(
                text = label,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-4).dp, y = (-9).dp)
                    .background(labelBackground)
                    .padding(horizontal = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = borderColor
            )
        }
        supportingText?.let { text ->
            Text(
                text = text,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) colorScheme.error else colorScheme.onSurfaceVariant
            )
        }
    }
}

private const val MAX_CURRENCY_DIGITS = 15

private fun String.toDecimalInput(): String {
    val filtered = filter { it.isDigit() || it == '.' }
    val firstDot = filtered.indexOf('.')
    if (firstDot == -1) return filtered
    return filtered.substring(0, firstDot + 1) +
        filtered.substring(firstDot + 1).replace(".", "")
}

/** Displays a digit string as "1.500.000". Cursor offsets are mapped around the separators. */
private object ThousandSeparatorTransformation : VisualTransformation {
    private const val SEPARATOR = '.'

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val count = digits.length
        val formatted = buildString {
            digits.forEachIndexed { index, char ->
                if (index > 0 && (count - index) % 3 == 0) append(SEPARATOR)
                append(char)
            }
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val original = offset.coerceAtMost(count)
                // Separators sitting before the digit at position original - 1.
                return original + (count - 1) / 3 - (count - original) / 3
            }

            override fun transformedToOriginal(offset: Int): Int {
                val transformed = offset.coerceIn(0, formatted.length)
                return transformed - formatted.take(transformed).count { it == SEPARATOR }
            }
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
