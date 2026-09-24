package com.diavolo.gogroceriesapp.core.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Base text field. Stateless: the caller owns [value].
 *
 * - Shows [errorText] in place of [helperText] when present.
 * - Shows a character counter when [maxLength] is set, and trims input to it
 *   (so pasting a long string is truncated rather than rejected).
 * - Set [isCurrency] for a whole-rupiah amount: number keyboard, digits only, shown with "."
 *   thousand separators. [value] stays the raw digit string (no separators), so
 *   `value.toLongOrNull()` keeps working.
 * - Set [isDecimal] for a decimal number (e.g. quantity): decimal keyboard, digits and a single
 *   '.'. [isCurrency] and [isDecimal] are mutually exclusive.
 * - Exposes the error message to accessibility services.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    prefix: String? = null,
    suffix: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    maxLength: Int? = null,
    isCurrency: Boolean = false,
    isDecimal: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = when {
        isCurrency -> KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
        isDecimal -> KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
        else -> KeyboardOptions.Default
    },
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = if (isCurrency) {
        AppTextFieldCurrency.visualTransformation
    } else {
        VisualTransformation.None
    },
    shape: Shape = MaterialTheme.shapes.medium,
) {
    require(!(isCurrency && isDecimal)) { "isCurrency and isDecimal are mutually exclusive." }
    val isError = errorText != null
    val hasSupportingRow = isError || helperText != null || maxLength != null

    OutlinedTextField(
        value = value,
        onValueChange = { new ->
            val filtered = when {
                isCurrency -> AppTextFieldCurrency.filter(new)
                isDecimal -> AppTextFieldDecimal.filter(new)
                else -> new
            }
            onValueChange(if (maxLength != null) filtered.take(maxLength) else filtered)
        },
        modifier = modifier
            .fillMaxWidth()
            .semantics { if (errorText != null) error(errorText) },
        enabled = enabled,
        readOnly = readOnly,
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        prefix = prefix?.let { { Text(it) } },
        suffix = suffix?.let { { Text(it) } },
        leadingIcon = leadingIcon?.let { icon -> { Icon(icon, contentDescription = null) } },
        trailingIcon = trailingIcon,
        supportingText = if (hasSupportingRow) {
            {
                Row(Modifier.fillMaxWidth()) {
                    Text(
                        text = errorText ?: helperText.orEmpty(),
                        modifier = Modifier.weight(1f),
                    )
                    if (maxLength != null) {
                        Text(
                            text = "${value.length} / $maxLength",
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .semantics {
                                    contentDescription = "${value.length} of $maxLength characters"
                                },
                        )
                    }
                }
            }
        } else null,
        isError = isError,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        shape = shape,
    )
}

/**
 * Currency filtering/formatting for [AppTextField], kept independent from [AppTextFieldInput].
 * `internal` (not `private`) so unit tests in this module can exercise it directly.
 */
internal object AppTextFieldCurrency {
    // Below Long.MAX_VALUE's 19 digits, so toLongOrNull() on the raw value can't overflow.
    private const val MAX_DIGITS = 15

    fun filter(input: String): String = input.filter(Char::isDigit).take(MAX_DIGITS)

    val visualTransformation: VisualTransformation = AppTextFieldThousandSeparatorTransformation
}

/** Decimal filtering for [AppTextField] (`isDecimal = true`): digits and a single dot. */
internal object AppTextFieldDecimal {
    fun filter(input: String): String {
        val filtered = input.filter { it.isDigit() || it == '.' }
        val firstDot = filtered.indexOf('.')
        if (firstDot == -1) return filtered
        return filtered.substring(0, firstDot + 1) +
            filtered.substring(firstDot + 1).replace(".", "")
    }
}

/** Displays a digit string as "1.500.000". Cursor offsets are mapped around the separators. */
private object AppTextFieldThousandSeparatorTransformation : VisualTransformation {
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
