package com.diavolo.gogroceriesapp.feature.activeshopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diavolo.gogroceriesapp.core.ui.LoadingStateContent
import com.diavolo.gogroceriesapp.core.ui.MessageStateContent
import com.diavolo.gogroceriesapp.domain.Money
import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.model.GroceryList
import com.diavolo.gogroceriesapp.domain.model.ListStatus

@Composable
fun ActiveShoppingRoute(
    listId: Long,
    onBackClick: () -> Unit,
    onShoppingFinished: () -> Unit,
    viewModel: ActiveShoppingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showFinishDialog by remember { mutableStateOf(false) }
    var pricingItem by remember { mutableStateOf<GroceryItem?>(null) }

    LaunchedEffect(listId) {
        viewModel.loadList(listId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ActiveShoppingEvent.ShoppingFinished -> {
                    showFinishDialog = false
                    onShoppingFinished()
                }
                ActiveShoppingEvent.PriceUpdated -> pricingItem = null
                is ActiveShoppingEvent.Message -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    ActiveShoppingScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onRetryClick = viewModel::retry,
        onToggleItem = viewModel::toggleItem,
        onPriceClick = { item ->
            viewModel.clearPriceUpdateError()
            pricingItem = item
        },
        onFinishClick = { showFinishDialog = true }
    )

    if (showFinishDialog) {
        FinishShoppingDialog(
            list = uiState.list,
            isFinishing = uiState.isFinishing,
            onDismiss = { showFinishDialog = false },
            onConfirm = viewModel::finishShopping
        )
    }

    pricingItem?.let { item ->
        ActualPriceDialog(
            item = item,
            isSaving = item.id in uiState.updatingItemIds,
            errorMessage = uiState.priceUpdateError,
            onDismiss = { pricingItem = null },
            onSave = { actualPrice ->
                viewModel.updateActualPrice(item, actualPrice)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveShoppingScreen(
    uiState: ActiveShoppingUiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onToggleItem: (GroceryItem) -> Unit,
    onPriceClick: (GroceryItem) -> Unit,
    onFinishClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val list = uiState.list

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = list?.name ?: "Shopping mode",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (list?.status == ListStatus.Active) {
                            Text(
                                text = "Active shopping",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (list?.status == ListStatus.Active && list.items.isNotEmpty()) {
                ShoppingBottomBar(
                    list = list,
                    actualCheckedSubtotal = uiState.actualCheckedSubtotal,
                    checkedItemsMissingPrice = uiState.checkedItemsMissingPrice,
                    isFinishing = uiState.isFinishing,
                    onFinishClick = onFinishClick
                )
            }
        }
    ) { contentPadding ->
        when {
            uiState.isLoading -> LoadingStateContent(contentPadding)
            uiState.errorMessage != null -> MessageStateContent(
                contentPadding = contentPadding,
                title = "Couldn't load shopping mode",
                message = uiState.errorMessage,
                actionLabel = "Try again",
                onActionClick = onRetryClick
            )
            uiState.isNotFound -> MessageStateContent(
                contentPadding = contentPadding,
                title = "List not found",
                message = "This shopping list may have been deleted.",
                actionLabel = "Go back",
                onActionClick = onBackClick
            )
            list?.status == ListStatus.Completed -> MessageStateContent(
                contentPadding = contentPadding,
                title = "Shopping completed",
                message = "This trip is already marked as completed.",
                actionLabel = "Go back",
                onActionClick = onBackClick
            )
            list != null -> ShoppingContent(
                uiState = uiState,
                contentPadding = contentPadding,
                onToggleItem = onToggleItem,
                onPriceClick = onPriceClick
            )
        }
    }
}

@Composable
private fun ShoppingContent(
    uiState: ActiveShoppingUiState,
    contentPadding: PaddingValues,
    onToggleItem: (GroceryItem) -> Unit,
    onPriceClick: (GroceryItem) -> Unit
) {
    val list = requireNotNull(uiState.list)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            end = 20.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "progress") {
            ShoppingProgressCard(
                list = list,
                estimatedTotal = uiState.estimatedTotal
            )
        }

        uiState.itemGroups.forEach { group ->
            item(key = "header-${group.categoryName}") {
                val checked = group.items.count(GroceryItem::isChecked)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = group.categoryName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$checked/${group.items.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(group.items, key = GroceryItem::id) { item ->
                ShoppingItemCard(
                    item = item,
                    isUpdating = item.id in uiState.updatingItemIds,
                    onToggleClick = { onToggleItem(item) },
                    onPriceClick = { onPriceClick(item) }
                )
            }
        }
    }
}

@Composable
private fun ShoppingProgressCard(
    list: GroceryList,
    estimatedTotal: Money
) {
    val checkedItems = list.items.count(GroceryItem::isChecked)
    val progress = checkedItems.toFloat() / list.items.size.coerceAtLeast(1)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$checkedItems of ${list.items.size} items",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Estimated list total $estimatedTotal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
private fun ShoppingItemCard(
    item: GroceryItem,
    isUpdating: Boolean,
    onToggleClick: () -> Unit,
    onPriceClick: () -> Unit
) {
    Card(
        onClick = onToggleClick,
        enabled = !isUpdating,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isChecked) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isUpdating) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                }
            } else {
                Checkbox(
                    checked = item.isChecked,
                    onCheckedChange = { onToggleClick() }
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (item.isChecked) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
                    color = if (item.isChecked) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "${formatQuantity(item.quantity)} ${item.unit.name.lowercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(
                onClick = onPriceClick,
                enabled = !isUpdating
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = item.actualPriceRupiah
                            ?.let(::Money)
                            ?.toString()
                            ?: "Set price",
                        style = MaterialTheme.typography.labelLarge
                    )
                    if (item.actualPriceRupiah != null) {
                        Text(
                            text = "Actual / unit",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShoppingBottomBar(
    list: GroceryList,
    actualCheckedSubtotal: Money,
    checkedItemsMissingPrice: Int,
    isFinishing: Boolean,
    onFinishClick: () -> Unit
) {
    val checkedItems = list.items.count(GroceryItem::isChecked)

    Surface(
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Actual checked subtotal",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = actualCheckedSubtotal.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "$checkedItems/${list.items.size} checked",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (checkedItemsMissingPrice > 0) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$checkedItemsMissingPrice checked ${if (checkedItemsMissingPrice == 1) "item needs" else "items need"} an actual price",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onFinishClick,
                enabled = !isFinishing,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (isFinishing) "Finishing..." else "Finish shopping")
            }
        }
    }
}

@Composable
private fun ActualPriceDialog(
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
        onDismissRequest = {
            if (!isSaving) onDismiss()
        },
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
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun FinishShoppingDialog(
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
            add(
                "$remaining ${if (remaining == 1) "item is" else "items are"} still unchecked."
            )
        }
        if (missingPrices > 0) {
            add(
                "$missingPrices purchased ${if (missingPrices == 1) "item has" else "items have"} no actual price."
            )
        }
        add("This trip will be marked completed.")
    }.joinToString("\n\n")

    AlertDialog(
        onDismissRequest = {
            if (!isFinishing) onDismiss()
        },
        icon = {
            Icon(Icons.Outlined.CheckCircle, contentDescription = null)
        },
        title = { Text("Finish shopping?") },
        text = {
            Text(finishMessage)
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isFinishing
            ) {
                Text(if (isFinishing) "Finishing..." else "Finish")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isFinishing
            ) {
                Text("Keep shopping")
            }
        }
    )
}

private fun formatQuantity(quantity: Double): String =
    if (quantity % 1.0 == 0.0) quantity.toLong().toString() else quantity.toString()
