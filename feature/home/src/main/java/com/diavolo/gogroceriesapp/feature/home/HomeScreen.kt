package com.diavolo.gogroceriesapp.feature.home

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons.Outlined
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diavolo.gogroceriesapp.domain.Money
import com.diavolo.gogroceriesapp.domain.model.GroceryList
import com.diavolo.gogroceriesapp.domain.model.ListStatus

@Composable
fun HomeRoute(
    onListClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateSheet by remember { mutableStateOf(false) }
    val openCreateSheet = {
        viewModel.clearCreationError()
        showCreateSheet = true
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is HomeEvent.ListCreated) showCreateSheet = false
        }
    }

    HomeScreen(
        uiState = uiState,
        onCreateListClick = openCreateSheet,
        onRetryClick = viewModel::retryLoading,
        onListClick = onListClick
    )

    if (showCreateSheet) {
        CreateListSheet(
            onDismiss = { showCreateSheet = false },
            isCreating = uiState.isCreating,
            errorMessage = uiState.creationError,
            onCreateList = viewModel::createList
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onCreateListClick: () -> Unit,
    onRetryClick: () -> Unit,
    onListClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "My shopping lists",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Plan smarter, shop easier",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (uiState.lists.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onCreateListClick,
                    icon = { Icon(Outlined.Add, contentDescription = null) },
                    text = { Text("New list") },
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                )
            }
        }
    ) { contentPadding ->
        when {
            uiState.isLoading -> LoadingContent(contentPadding)
            uiState.loadError != null -> LoadErrorContent(
                contentPadding = contentPadding,
                message = uiState.loadError,
                onRetryClick = onRetryClick
            )
            uiState.lists.isEmpty() -> EmptyListsContent(
                contentPadding = contentPadding,
                onCreateListClick = onCreateListClick
            )
            else -> HomeContent(
                lists = uiState.lists,
                contentPadding = contentPadding,
                onListClick = onListClick
            )
        }
    }
}

@Composable
private fun LoadErrorContent(
    contentPadding: PaddingValues,
    message: String,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "We couldn't load your lists",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onRetryClick) {
            Text("Try again")
        }
    }
}

@Composable
private fun LoadingContent(contentPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyListsContent(
    contentPadding: PaddingValues,
    onCreateListClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(88.dp),
            shape = RoundedCornerShape(28.dp),
            color = colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.FormatListBulleted,
                contentDescription = null,
                modifier = Modifier.padding(24.dp),
                tint = colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Your next trip starts here",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Create a list to organize your groceries, track progress, and stay on budget.",
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onCreateListClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Outlined.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Create your first list")
        }
    }
}

@Composable
private fun HomeContent(
    lists: List<GroceryList>,
    contentPadding: PaddingValues,
    onListClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            end = 20.dp,
            bottom = contentPadding.calculateBottomPadding() + 88.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Your lists",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        items(count = lists.size, key = { index -> lists[index].id }) { index ->
            GroceryListCard(
                list = lists[index],
                onClick = { onListClick(lists[index].id) }
            )
        }
    }
}

@Composable
private fun GroceryListCard(
    list: GroceryList,
    onClick: () -> Unit
) {
    val totalItems = list.items.size
    val completedItems = list.items.count { it.isChecked }
    val progress = if (totalItems == 0) 0f else completedItems.toFloat() / totalItems

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = list.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "$totalItems ${if (totalItems == 1) "item" else "items"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                ListStatusChip(status = list.status)
            }
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = colorScheme.primary,
                trackColor = colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (totalItems == 0) "Add items when you're ready" else "$completedItems of $totalItems items complete",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurfaceVariant
            )
            list.budgetRupiah?.let { budget ->
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = colorScheme.surfaceVariant)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Budget ${Money(budget)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ListStatusChip(status: ListStatus) {
    val (label, icon) = when (status) {
        ListStatus.Draft -> "Draft" to Icons.AutoMirrored.Outlined.FormatListBulleted
        ListStatus.Active -> "Active" to Icons.AutoMirrored.Outlined.ReceiptLong
        ListStatus.Completed -> "Completed" to Outlined.CheckCircle
        ListStatus.Archived -> "Archived" to Icons.AutoMirrored.Outlined.ReceiptLong
    }
    AssistChip(
        onClick = {},
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (status == ListStatus.Active) {
                colorScheme.primaryContainer
            } else {
                colorScheme.surfaceVariant
            },
            labelColor = if (status == ListStatus.Active) {
                colorScheme.onPrimaryContainer
            } else {
                colorScheme.onSurfaceVariant
            },
            leadingIconContentColor = if (status == ListStatus.Active) {
                colorScheme.onPrimaryContainer
            } else {
                colorScheme.onSurfaceVariant
            }
        ),
        border = null
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateListSheet(
    onDismiss: () -> Unit,
    isCreating: Boolean,
    errorMessage: String?,
    onCreateList: (name: String, budgetRupiah: Long?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var showNameError by remember { mutableStateOf(false) }
    val parsedBudget = budget.trim().toLongOrNull()
    val budgetIsInvalid = budget.isNotBlank() && (parsedBudget == null || parsedBudget < 0)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Create a new list",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Give this trip a clear name. You can add groceries next.",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (it.isNotBlank()) showNameError = false
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("List name") },
                placeholder = { Text("e.g. Weekly groceries") },
                singleLine = true,
                isError = showNameError,
                supportingText = if (showNameError) {
                    { Text("Enter a name for your list.") }
                } else {
                    null
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    focusedLabelColor = colorScheme.primary
                )
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = budget,
                onValueChange = { budget = it.filter(Char::isDigit) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Budget (optional)") },
                prefix = { Text("Rp ") },
                placeholder = { Text("0") },
                singleLine = true,
                isError = budgetIsInvalid,
                supportingText = {
                    Text(if (budgetIsInvalid) "Enter a valid budget." else "Set a limit for this shopping trip.")
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    focusedLabelColor = colorScheme.primary
                )
            )
            Spacer(Modifier.height(24.dp))
            errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.error
                )
                Spacer(Modifier.height(12.dp))
            }
            Button(
                onClick = {
                    if (name.isBlank()) {
                        showNameError = true
                    } else if (!budgetIsInvalid) {
                        onCreateList(name, parsedBudget)
                    }
                },
                enabled = !isCreating,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                )
            ) {
                Text(if (isCreating) "Creating list..." else "Create list")
            }
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Cancel")
            }
        }
    }
}
