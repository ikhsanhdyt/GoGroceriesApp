package com.diavolo.gogroceriesapp.core.ui.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.diavolo.gogroceriesapp.core.ui.AppTextField

@PreviewLightDark
@Composable
private fun AppTextFieldGalleryPreview() {
    PreviewSurface { AppTextFieldGallery() }
}

@Preview(name = "Narrow width", widthDp = 220)
@Composable
private fun AppTextFieldNarrowPreview() {
    PreviewSurface { AppTextFieldGallery() }
}

@Preview(name = "Large font", fontScale = 1.5f)
@Composable
private fun AppTextFieldLargeFontPreview() {
    PreviewSurface { AppTextFieldGallery() }
}

@Composable
private fun AppTextFieldGallery() {
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Empty, with label and placeholder only.
        StatefulField(
            initial = "",
            label = "Item name",
            placeholder = "e.g. Organic bananas"
        )

        // Filled, with a leading icon.
        StatefulField(
            initial = "Bananas",
            label = "Search items",
            leadingIcon = Icons.Default.Search
        )

        // Filled, with a clear (trailing) action.
        StatefulField(
            initial = "Weekly groceries",
            label = "List name",
            showClearAction = true
        )

        // Helper text, no error.
        StatefulField(
            initial = "",
            label = "Notes",
            helperText = "Visible only to you."
        )

        // Error takes over the supporting row.
        StatefulField(
            initial = "not-a-number",
            label = "Estimated price",
            helperText = "Used to compute the trip total.",
            errorText = "Enter a valid whole-rupiah amount."
        )

        // Character counter via maxLength, combined with an error.
        StatefulField(
            initial = "This note is getting a little too long",
            label = "Note",
            errorText = "Keep it under 40 characters.",
            maxLength = 40
        )

        // Currency: filled, shows thousand separators while the value stays raw digits.
        StatefulField(
            initial = "1500000",
            label = "Budget",
            isCurrency = true,
            prefix = "Rp "
        )
        // Decimal: quantity-style, digits and a single dot.
        StatefulField(
            initial = "1.5",
            label = "Quantity",
            isDecimal = true
        )

        // Multiline.
        StatefulField(
            initial = "Remember to check the loyalty card discount at checkout.",
            label = "Notes",
            singleLine = false,
            minLines = 3,
            maxLines = 5
        )

        // Currency: empty, with a "Rp " prefix and placeholder.
        StatefulField(
            initial = "",
            label = "Estimated price",
            isCurrency = true,
            prefix = "Rp ",
            placeholder = "0"
        )


        // Custom shape.
        StatefulField(
            initial = "Rounded corners",
            label = "Custom shape",
            shape = RoundedCornerShape(50)
        )

        // Read-only.
        StatefulField(
            initial = "Piece",
            label = "Unit",
            readOnly = true
        )

        // Disabled.
        StatefulField(
            initial = "Can't edit this",
            label = "Disabled",
            enabled = false
        )
    }
}

/** Keeps its own state so every field can be edited in Android Studio's interactive mode. */
@Composable
private fun StatefulField(
    initial: String,
    label: String,
    placeholder: String? = null,
    prefix: String? = null,
    suffix: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    showClearAction: Boolean = false,
    maxLength: Int? = null,
    isCurrency: Boolean = false,
    isDecimal: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.medium
) {
    var value by remember { mutableStateOf(initial) }
    AppTextField(
        value = value,
        onValueChange = { value = it },
        label = label,
        placeholder = placeholder,
        prefix = prefix,
        suffix = suffix,
        helperText = helperText,
        errorText = errorText,
        leadingIcon = leadingIcon,
        trailingIcon = if (showClearAction && value.isNotEmpty()) {
            {
                IconButton(onClick = { value = "" }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        } else {
            null
        },
        maxLength = maxLength,
        isCurrency = isCurrency,
        isDecimal = isDecimal,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        shape = shape
    )
}

/** `core` cannot see the app theme, so use a plain Material3 scheme for the preview surface. */
@Composable
private fun PreviewSurface(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    ) {
        Surface(color = MaterialTheme.colorScheme.surface, content = content)
    }
}
