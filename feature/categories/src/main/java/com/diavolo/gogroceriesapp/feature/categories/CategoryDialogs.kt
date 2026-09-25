package com.diavolo.gogroceriesapp.feature.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.diavolo.gogroceriesapp.core.ui.AppTextField
import com.diavolo.gogroceriesapp.core.ui.categoryColor
import com.diavolo.gogroceriesapp.domain.model.Category

private const val CATEGORY_NAME_MAX_LENGTH = 30

internal data class CategoryColorOption(val hex: String, val label: String)

/** The first seven match the default categories' colors. */
internal val CATEGORY_COLOR_OPTIONS = listOf(
    CategoryColorOption("#4F8A5B", "Green"),
    CategoryColorOption("#4A90A4", "Teal"),
    CategoryColorOption("#C75B5B", "Red"),
    CategoryColorOption("#C98B4A", "Orange"),
    CategoryColorOption("#5B7DB1", "Blue"),
    CategoryColorOption("#A77A3D", "Brown"),
    CategoryColorOption("#7A6F9B", "Purple"),
    CategoryColorOption("#2E7D6B", "Dark green"),
    CategoryColorOption("#C2627F", "Pink"),
    CategoryColorOption("#8A8F3C", "Olive"),
    CategoryColorOption("#B5563A", "Rust"),
    CategoryColorOption("#6B7280", "Gray")
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun CategoryFormSheet(
    category: Category?,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (name: String, colorHex: String) -> Unit
) {
    val isEditing = category != null
    var name by remember { mutableStateOf(category?.name.orEmpty()) }
    var selectedColor by remember {
        mutableStateOf(category?.colorHex ?: CATEGORY_COLOR_OPTIONS.first().hex)
    }
    var submitted by remember { mutableStateOf(false) }
    val colorOptions = remember(category) {
        val current = category?.colorHex
        if (current == null || CATEGORY_COLOR_OPTIONS.any { it.hex.equals(current, ignoreCase = true) }) {
            CATEGORY_COLOR_OPTIONS
        } else {
            listOf(CategoryColorOption(current, "Current color")) + CATEGORY_COLOR_OPTIONS
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (!isSaving) onDismiss()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = if (isEditing) "Edit category" else "New category",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (isEditing) {
                    "Changes apply to every item in this category."
                } else {
                    "New categories appear after the existing ones in your lists."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            AppTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = "Category name",
                placeholder = "e.g. Drinks",
                maxLength = CATEGORY_NAME_MAX_LENGTH,
                errorText = if (submitted && name.isBlank()) "Enter a category name." else null
            )
            Spacer(Modifier.height(20.dp))

            Text(
                text = "Color",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                colorOptions.forEach { option ->
                    ColorSwatch(
                        color = categoryColor(option.hex),
                        label = option.label,
                        selected = option.hex.equals(selectedColor, ignoreCase = true),
                        onClick = { selectedColor = option.hex }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))

            errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(12.dp))
            }
            Button(
                onClick = {
                    submitted = true
                    if (name.isNotBlank()) onSave(name, selectedColor)
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    when {
                        isSaving -> "Saving..."
                        isEditing -> "Save changes"
                        else -> "Add category"
                    }
                )
            }
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = label }
            .border(
                width = 2.dp,
                color = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                shape = CircleShape
            )
            .padding(4.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
internal fun DeleteCategoryDialog(
    category: Category,
    isDeleting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!isDeleting) onDismiss()
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Delete ${category.name}?") },
        text = {
            Column {
                Text("Items in this category won't be deleted. They'll move to Other.")
                errorMessage?.let { message ->
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isDeleting,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(if (isDeleting) "Deleting..." else "Delete")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting
            ) {
                Text("Cancel")
            }
        }
    )
}
