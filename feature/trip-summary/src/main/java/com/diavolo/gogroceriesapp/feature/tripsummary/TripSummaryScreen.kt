package com.diavolo.gogroceriesapp.feature.tripsummary

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diavolo.gogroceriesapp.core.ui.LoadingStateContent
import com.diavolo.gogroceriesapp.core.ui.MessageStateContent
import com.diavolo.gogroceriesapp.domain.Money
import com.diavolo.gogroceriesapp.domain.model.GroceryItem
import com.diavolo.gogroceriesapp.domain.model.GroceryList
import com.diavolo.gogroceriesapp.domain.model.TripSummary
import kotlin.math.abs

@Composable
fun TripSummaryRoute(
    listId: Long,
    onBackClick: () -> Unit,
    onDoneClick: () -> Unit,
    viewModel: TripSummaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(listId) {
        viewModel.loadList(listId)
    }

    TripSummaryScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onDoneClick = onDoneClick,
        onRetryClick = viewModel::retry
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripSummaryScreen(
    uiState: TripSummaryUiState,
    onBackClick: () -> Unit,
    onDoneClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.list?.name ?: "Trip summary",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { contentPadding ->
        when {
            uiState.isLoading -> LoadingStateContent(contentPadding)
            uiState.errorMessage != null -> MessageStateContent(
                contentPadding = contentPadding,
                title = "Couldn't load this summary",
                message = uiState.errorMessage,
                actionLabel = "Try again",
                onActionClick = onRetryClick
            )
            uiState.isNotFound -> MessageStateContent(
                contentPadding = contentPadding,
                title = "Trip not found",
                message = "This shopping list may have been deleted.",
                actionLabel = "Go back",
                onActionClick = onBackClick
            )
            uiState.isUnavailable -> MessageStateContent(
                contentPadding = contentPadding,
                title = "Trip is not completed",
                message = "Finish shopping before opening its spending summary.",
                actionLabel = "Go back",
                onActionClick = onBackClick
            )
            uiState.list != null && uiState.summary != null -> SummaryContent(
                list = uiState.list,
                summary = uiState.summary,
                contentPadding = contentPadding,
                onDoneClick = onDoneClick
            )
        }
    }
}

@Composable
private fun SummaryContent(
    list: GroceryList,
    summary: TripSummary,
    contentPadding: PaddingValues,
    onDoneClick: () -> Unit
) {
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
        item(key = "hero") {
            ActualSpentCard(summary)
        }

        item(key = "totals") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryMetricCard(
                    label = "Estimated",
                    value = summary.estimatedPurchasedTotal.toString(),
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricCard(
                    label = "Budget",
                    value = summary.budget?.toString() ?: "Not set",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item(key = "budget") {
            BudgetResultCard(summary)
        }

        if (summary.missingActualPriceCount > 0) {
            item(key = "missing-prices") {
                MissingPriceCard(summary.missingActualPriceCount)
            }
        }

        item(key = "purchased-header") {
            SectionHeader(
                title = "Purchased items",
                count = summary.purchasedItems.size
            )
        }

        if (summary.purchasedItems.isEmpty()) {
            item(key = "no-purchases") {
                EmptySectionCard("No items were marked as purchased.")
            }
        } else {
            items(summary.purchasedItems, key = GroceryItem::id) { item ->
                PurchasedItemCard(item)
            }
        }

        if (summary.skippedItems.isNotEmpty()) {
            item(key = "skipped-header") {
                SectionHeader(
                    title = "Skipped items",
                    count = summary.skippedItems.size
                )
            }
            items(summary.skippedItems, key = GroceryItem::id) { item ->
                SkippedItemCard(item)
            }
        }

        item(key = "done") {
            Button(
                onClick = onDoneClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Back to my lists")
            }
        }
    }
}

@Composable
private fun ActualSpentCard(summary: TripSummary) {
    val estimateDifference = summary.actualSpent.rupiah -
        summary.estimatedPurchasedTotal.rupiah

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp).size(28.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Actual spent",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = summary.actualSpent.toString(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = when {
                    estimateDifference > 0 ->
                        "${Money(estimateDifference)} above the purchased estimate"
                    estimateDifference < 0 ->
                        "${Money(abs(estimateDifference))} below the purchased estimate"
                    else -> "Matched the purchased estimate"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SummaryMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BudgetResultCard(summary: TripSummary) {
    val remaining = summary.budgetRemainingRupiah
    val (title, message, positive) = when {
        remaining == null -> Triple(
            "No budget comparison",
            "This trip did not have a budget.",
            true
        )
        remaining > 0 -> Triple(
            "Under budget",
            "${Money(remaining)} remaining",
            true
        )
        remaining < 0 -> Triple(
            "Over budget",
            "${Money(abs(remaining))} over",
            false
        )
        else -> Triple("On budget", "Spent exactly the planned budget", true)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (positive) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.AccountBalanceWallet,
                contentDescription = null
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold)
                Text(text = message, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun MissingPriceCard(count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Info, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(
                text = "$count purchased ${if (count == 1) "item has" else "items have"} no actual price, so actual spending may be incomplete.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PurchasedItemCard(item: GroceryItem) {
    val actualSubtotal = item.actualPriceRupiah?.let { price ->
        Money.fromRupiah(price * item.quantity)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "${formatQuantity(item.quantity)} ${item.unit.name.lowercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = actualSubtotal?.toString() ?: "No actual price",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SkippedItemCard(item: GroceryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Skipped",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptySectionCard(message: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = message,
            modifier = Modifier.padding(20.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatQuantity(quantity: Double): String =
    if (quantity % 1.0 == 0.0) quantity.toLong().toString() else quantity.toString()
