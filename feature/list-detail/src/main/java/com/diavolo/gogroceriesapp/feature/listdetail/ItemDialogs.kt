package com.diavolo.gogroceriesapp.feature.listdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.diavolo.gogroceriesapp.domain.model.Category
import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.model.UnitOfMeasure

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ItemFormSheet(
    item: GroceryItem?,
    categories: List<Category>,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        quantity: Double,
        unit: UnitOfMeasure,
        categoryId: Long?,
        estimatedPriceRupiah: Long?
    ) -> Unit
) {
    val itemKey = item?.id ?: 0L
    val isEditing = item != null
    var name by remember(itemKey) { mutableStateOf(item?.name.orEmpty()) }
    var quantity by remember(itemKey) {
        mutableStateOf(item?.quantity?.let(::formatQuantity) ?: "1")
    }
    var estimatedPrice by remember(itemKey) {
        mutableStateOf(item?.estimatedPriceRupiah?.toString().orEmpty())
    }
    var selectedUnit by remember(itemKey) {
        mutableStateOf(item?.unit ?: UnitOfMeasure.PIECE)
    }
    var selectedCategoryId by remember(itemKey) { mutableStateOf(item?.categoryId) }
    var unitMenuExpanded by remember { mutableStateOf(false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }

    val parsedQuantity = quantity.toDoubleOrNull()
    val quantityIsInvalid = parsedQuantity == null || parsedQuantity <= 0
    val parsedPrice = estimatedPrice.toLongOrNull()
    val priceIsInvalid = estimatedPrice.isNotBlank() && parsedPrice == null

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = if (isEditing) "Edit grocery item" else "Add a grocery item",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (isEditing) {
                    "Update the item details for this shopping list."
                } else {
                    "Add the essentials now. You can refine the item later."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Item name") },
                placeholder = { Text("e.g. Organic bananas") },
                singleLine = true,
                isError = submitted && name.isBlank(),
                supportingText = if (submitted && name.isBlank()) {
                    { Text("Enter an item name.") }
                } else {
                    null
                },
                colors = addItemTextFieldColors()
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { value ->
                        quantity = value.filter { it.isDigit() || it == '.' }
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Quantity") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = submitted && quantityIsInvalid,
                    supportingText = if (submitted && quantityIsInvalid) {
                        { Text("Enter more than 0.") }
                    } else {
                        null
                    },
                    colors = addItemTextFieldColors()
                )

                ExposedDropdownMenuBox(
                    expanded = unitMenuExpanded,
                    onExpandedChange = { unitMenuExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedUnit.displayName(),
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(unitMenuExpanded)
                        },
                        colors = addItemTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = unitMenuExpanded,
                        onDismissRequest = { unitMenuExpanded = false }
                    ) {
                        UnitOfMeasure.entries.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit.displayName()) },
                                onClick = {
                                    selectedUnit = unit
                                    unitMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            if (categories.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = categoryMenuExpanded,
                    onExpandedChange = { categoryMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = categories
                            .firstOrNull { it.id == selectedCategoryId }
                            ?.name
                            ?: "Other",
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(categoryMenuExpanded)
                        },
                        colors = addItemTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Other") },
                            onClick = {
                                selectedCategoryId = null
                                categoryMenuExpanded = false
                            }
                        )
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategoryId = category.id
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = estimatedPrice,
                onValueChange = { estimatedPrice = it.filter(Char::isDigit) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Estimated price (optional)") },
                prefix = { Text("Rp ") },
                placeholder = { Text("0") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = submitted && priceIsInvalid,
                supportingText = {
                    Text(
                        if (submitted && priceIsInvalid) {
                            "Enter a valid whole-rupiah amount."
                        } else {
                            "Estimated price per unit."
                        }
                    )
                },
                colors = addItemTextFieldColors()
            )

            errorMessage?.let { message ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    submitted = true
                    if (name.isNotBlank() && !quantityIsInvalid && !priceIsInvalid) {
                        onSave(
                            name,
                            requireNotNull(parsedQuantity),
                            selectedUnit,
                            selectedCategoryId,
                            parsedPrice
                        )
                    }
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
                        isSaving && isEditing -> "Saving changes..."
                        isSaving -> "Adding item..."
                        isEditing -> "Save changes"
                        else -> "Add item"
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
internal fun DeleteItemDialog(
    item: GroceryItem,
    isDeleting: Boolean,
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
        title = { Text("Delete ${item.name}?") },
        text = {
            Text("This item will be removed from the shopping list.")
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

@Composable
private fun addItemTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary
)

private fun UnitOfMeasure.displayName(): String = name
    .lowercase()
    .replaceFirstChar(Char::uppercase)

internal fun formatQuantity(quantity: Double): String =
    if (quantity % 1.0 == 0.0) quantity.toLong().toString() else quantity.toString()
