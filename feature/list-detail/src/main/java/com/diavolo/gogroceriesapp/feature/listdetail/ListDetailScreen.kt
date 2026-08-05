package com.diavolo.gogroceriesapp.feature.listdetail

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
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diavolo.gogroceriesapp.core.ui.LoadingStateContent
import com.diavolo.gogroceriesapp.core.ui.MessageStateContent
import com.diavolo.gogroceriesapp.domain.Money
import com.diavolo.gogroceriesapp.domain.model.Category
import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.model.GroceryList
import com.diavolo.gogroceriesapp.domain.model.ListStatus
import com.diavolo.gogroceriesapp.domain.model.UnitOfMeasure

@Composable
fun ListDetailRoute(
    listId: Long,
    onBackClick: () -> Unit,
    onShoppingStarted: (Long) -> Unit,
    onTripSummaryClick: (Long) -> Unit,
    viewModel: ListDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddItemSheet by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<GroceryItem?>(null) }
    var deletingItem by remember { mutableStateOf<GroceryItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(listId) {
        viewModel.loadList(listId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ListDetailEvent.ItemAdded -> showAddItemSheet = false
                ListDetailEvent.ItemUpdated -> editingItem = null
                ListDetailEvent.ItemDeleted -> deletingItem = null
                is ListDetailEvent.ShoppingStarted -> onShoppingStarted(event.listId)
                is ListDetailEvent.Message -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    ListDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onRetryClick = viewModel::retry,
        snackbarHostState = snackbarHostState,
        onToggleItem = viewModel::toggleItem,
        onEditItem = { item ->
            viewModel.clearEditItemError()
            editingItem = item
        },
        onDeleteItem = { item -> deletingItem = item },
        onStartShoppingClick = viewModel::startShopping,
        onTripSummaryClick = { onTripSummaryClick(listId) },
        onAddItemClick = {
            viewModel.clearAddItemError()
            showAddItemSheet = true
        }
    )

    if (showAddItemSheet) {
        ItemFormSheet(
            item = null,
            categories = uiState.categories,
            isSaving = uiState.isAddingItem,
            errorMessage = uiState.addItemError,
            onDismiss = { showAddItemSheet = false },
            onSave = viewModel::addItem
        )
    }

    editingItem?.let { item ->
        ItemFormSheet(
            item = item,
            categories = uiState.categories,
            isSaving = item.id in uiState.updatingItemIds,
            errorMessage = uiState.editItemError,
            onDismiss = { editingItem = null },
            onSave = { name, quantity, unit, categoryId, estimatedPrice ->
                viewModel.updateItem(
                    item = item,
                    name = name,
                    quantity = quantity,
                    unit = unit,
                    categoryId = categoryId,
                    estimatedPriceRupiah = estimatedPrice
                )
            }
        )
    }

    deletingItem?.let { item ->
        DeleteItemDialog(
            item = item,
            isDeleting = item.id in uiState.updatingItemIds,
            onDismiss = { deletingItem = null },
            onConfirm = { viewModel.deleteItem(item) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    uiState: ListDetailUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    onToggleItem: (GroceryItem) -> Unit,
    onEditItem: (GroceryItem) -> Unit,
    onDeleteItem: (GroceryItem) -> Unit,
    onStartShoppingClick: () -> Unit,
    onTripSummaryClick: () -> Unit,
    onAddItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = uiState.list?.name ?: "List details",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (
                uiState.list?.items?.isNotEmpty() == true &&
                uiState.list.status != ListStatus.Completed &&
                uiState.list.status != ListStatus.Archived
            ) {
                ExtendedFloatingActionButton(
                    onClick = onAddItemClick,
                    icon = {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                    },
                    text = { Text("Add item") }
                )
            }
        }
    ) { contentPadding ->
        when {
            uiState.isLoading -> LoadingStateContent(contentPadding)
            uiState.errorMessage != null -> MessageStateContent(
                contentPadding = contentPadding,
                title = "Couldn't load this list",
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
            uiState.list != null -> DetailContent(
                uiState = uiState,
                contentPadding = contentPadding,
                onAddItemClick = onAddItemClick,
                onToggleItem = onToggleItem,
                onEditItem = onEditItem,
                onDeleteItem = onDeleteItem,
                onStartShoppingClick = onStartShoppingClick,
                onTripSummaryClick = onTripSummaryClick
            )
        }
    }
}

@Composable
private fun DetailContent(
    uiState: ListDetailUiState,
    contentPadding: PaddingValues,
    onAddItemClick: () -> Unit,
    onToggleItem: (GroceryItem) -> Unit,
    onEditItem: (GroceryItem) -> Unit,
    onDeleteItem: (GroceryItem) -> Unit,
    onStartShoppingClick: () -> Unit,
    onTripSummaryClick: () -> Unit
) {
    val list = requireNotNull(uiState.list)
    val isReadOnly = list.status == ListStatus.Completed ||
        list.status == ListStatus.Archived

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
        item(key = "summary") {
            ListSummaryCard(
                list = list,
                estimatedTotal = uiState.estimatedTotal
            )
        }

        if (uiState.itemGroups.isEmpty()) {
            item(key = "empty") {
                EmptyItemsCard(
                    canAddItems = !isReadOnly,
                    onAddItemClick = onAddItemClick
                )
            }
        } else {
            uiState.itemGroups.forEach { group ->
                item(key = "header-${group.categoryName}") {
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
                            text = "${group.items.size} ${if (group.items.size == 1) "item" else "items"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(group.items, key = GroceryItem::id) { item ->
                    GroceryItemCard(
                        item = item,
                        isUpdating = item.id in uiState.updatingItemIds,
                        isReadOnly = isReadOnly,
                        onToggleClick = { onToggleItem(item) },
                        onEditClick = { onEditItem(item) },
                        onDeleteClick = { onDeleteItem(item) }
                    )
                }
            }
        }

        if (
            list.items.isNotEmpty() &&
            (list.status == ListStatus.Draft || list.status == ListStatus.Active)
        ) {
            item(key = "shopping-action") {
                Button(
                    onClick = onStartShoppingClick,
                    enabled = !uiState.isStartingShopping,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Outlined.ShoppingCart, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        when {
                            uiState.isStartingShopping -> "Opening shopping mode..."
                            list.status == ListStatus.Active -> "Continue shopping"
                            else -> "Start shopping"
                        }
                    )
                }
            }
        }

        if (list.status == ListStatus.Completed) {
            item(key = "trip-summary-action") {
                Button(
                    onClick = onTripSummaryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ReceiptLong, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("View trip summary")
                }
            }
        }
    }
}

@Composable
private fun ListSummaryCard(
    list: GroceryList,
    estimatedTotal: Money
) {
    val totalItems = list.items.size
    val checkedItems = list.items.count(GroceryItem::isChecked)
    val progress = if (totalItems == 0) 0f else checkedItems.toFloat() / totalItems

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
                        text = "Estimated total",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = estimatedTotal.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                StatusBadge(list.status)
            }

            Spacer(Modifier.height(18.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "$checkedItems of $totalItems items complete",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            list.budgetRupiah?.let { budget ->
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Budget ${Money(budget)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: ListStatus) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        Text(
            text = status.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun EmptyItemsCard(
    canAddItems: Boolean,
    onAddItemClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.FormatListBulleted,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "No groceries yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Items you add to this list will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (canAddItems) {
                Spacer(Modifier.height(20.dp))
                Button(onClick = onAddItemClick) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add first item")
                }
            }
        }
    }
}

@Composable
private fun GroceryItemCard(
    item: GroceryItem,
    isUpdating: Boolean,
    isReadOnly: Boolean,
    onToggleClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showActions by remember { mutableStateOf(false) }

    Card(
        onClick = onToggleClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isUpdating && !isReadOnly,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isUpdating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
                Checkbox(
                    checked = item.isChecked,
                    onCheckedChange = { onToggleClick() },
                    enabled = !isReadOnly
                )
            }
            Spacer(Modifier.width(14.dp))
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
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "${formatQuantity(item.quantity)} ${item.unit.name.lowercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = item.estimatedPriceRupiah?.let(::Money)?.toString() ?: "No estimate",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!isReadOnly) {
                Box {
                    IconButton(
                        onClick = { showActions = true },
                        enabled = !isUpdating
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "Actions for ${item.name}"
                        )
                    }
                    DropdownMenu(
                        expanded = showActions,
                        onDismissRequest = { showActions = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Edit, contentDescription = null)
                            },
                            onClick = {
                                showActions = false
                                onEditClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Delete, contentDescription = null)
                            },
                            onClick = {
                                showActions = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }
        }
    }
}
