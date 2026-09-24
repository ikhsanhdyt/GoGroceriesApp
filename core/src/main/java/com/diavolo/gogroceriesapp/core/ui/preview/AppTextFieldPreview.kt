package com.diavolo.gogroceriesapp.core.ui.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.diavolo.gogroceriesapp.core.ui.AppTextFieldInput

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTextFieldGallery() {
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        StatefulField(
            initial = "",
            label = "List name",
            placeholder = "e.g. Weekly groceries"
        )
        StatefulField(
            initial = "Weekly groceries",
            label = "List name"
        )
        StatefulField(
            initial = "",
            label = "Budget (optional)",
            input = AppTextFieldInput.Currency,
            prefix = "Rp ",
            placeholder = "0",
            supportingText = "Set a limit for this shopping trip."
        )
        StatefulField(
            initial = "1500000",
            label = "Budget (optional)",
            input = AppTextFieldInput.Currency,
            prefix = "Rp "
        )
        StatefulField(
            initial = "1.5",
            label = "Quantity",
            input = AppTextFieldInput.Decimal,
            suffix = "kg"
        )
        StatefulField(
            initial = "Piece",
            label = "Unit",
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) }
        )
        StatefulField(
            initial = "abc",
            label = "Budget (optional)",
            isError = true,
            supportingText = "Enter a valid budget."
        )
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
    input: AppTextFieldInput = AppTextFieldInput.Text,
    placeholder: String? = null,
    prefix: String? = null,
    suffix: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    var value by remember { mutableStateOf(initial) }
    AppTextField(
        value = value,
        onValueChange = { value = it },
        label = label,
        modifier = Modifier.fillMaxWidth(),
        input = input,
        placeholder = placeholder,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        isError = isError,
        enabled = enabled,
        readOnly = readOnly,
        trailingIcon = trailingIcon
    )
}

/** `core` cannot see the app theme, so match the surface color the label notch is drawn with. */
@Composable
private fun PreviewSurface(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    ) {
        Surface(color = MaterialTheme.colorScheme.surface, content = content)
    }
}
