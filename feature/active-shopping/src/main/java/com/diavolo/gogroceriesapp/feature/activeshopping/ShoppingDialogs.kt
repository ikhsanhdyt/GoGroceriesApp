package com.diavolo.gogroceriesapp.feature.activeshopping

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.diavolo.gogroceriesapp.domain.Money
import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.model.GroceryList

@Composable
internal fun ActualPriceDialog(
    item: GroceryItem,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (Long?) -> Unit
) {
    var price by remember(item.id, item.actualPriceRupiah) {
        mutableStateOf(item.actualPriceRupiah?.toString().orEmpty())
    }
    var submitted by remember(item.id) { mutableStateOf(false) }
    val parsedPrice = price.toLongOrNull()
    val priceIsInvalid = price.isBlank() || parsedPrice == null

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text("Actual price for ${item.name}") },
        text = {
            Column {
                Text(
                    text = "Enter the price per ${item.unit.name.lowercase()}. Quantity: ${formatQuantity(item.quantity)}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Actual unit price") },
                    prefix = { Text("Rp ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = submitted && priceIsInvalid,
                    supportingText = {
                        when {
                            submitted && priceIsInvalid -> Text("Enter a valid whole-rupiah amount.")
                            parsedPrice != null -> Text(
                                "Item subtotal ${Money.fromRupiah(parsedPrice * item.quantity)}"
                            )
                            else -> Text("Used for the completed-trip total.")
                        }
                    }
                )
                errorMessage?.let { message ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (item.actualPriceRupiah != null) {
                    TextButton(
                        onClick = { onSave(null) },
                        enabled = !isSaving,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Clear actual price")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    submitted = true
                    if (!priceIsInvalid) onSave(parsedPrice)
                },
                enabled = !isSaving
            ) {
                Text(if (isSaving) "Saving..." else "Save price")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancel")
            }
        }
    )
}

@Composable
internal fun FinishShoppingDialog(
    list: GroceryList?,
    isFinishing: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val remaining = list?.items?.count { !it.isChecked } ?: 0
    val missingPrices = list?.items?.count {
        it.isChecked && it.actualPriceRupiah == null
    } ?: 0
    val finishMessage = buildList {
        if (remaining == 0) {
            add("All items are checked.")
        } else {
            add("$remaining ${if (remaining == 1) "item is" else "items are"} still unchecked.")
        }
        if (missingPrices > 0) {
            add("$missingPrices purchased ${if (missingPrices == 1) "item has" else "items have"} no actual price.")
        }
        add("This trip will be marked completed.")
    }.joinToString("\n\n")

    AlertDialog(
        onDismissRequest = { if (!isFinishing) onDismiss() },
        icon = {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        },
        title = { Text("Finish shopping?") },
        text = { Text(finishMessage) },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isFinishing) {
                Text(if (isFinishing) "Finishing..." else "Finish")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isFinishing) {
                Text("Keep shopping")
            }
        }
    )
}
