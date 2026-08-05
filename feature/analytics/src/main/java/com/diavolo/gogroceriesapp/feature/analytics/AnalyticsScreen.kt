package com.diavolo.gogroceriesapp.feature.analytics

import android.graphics.Color.parseColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diavolo.gogroceriesapp.domain.Money
import com.diavolo.gogroceriesapp.domain.model.BudgetAnalytics
import com.diavolo.gogroceriesapp.domain.model.CategorySpending
import com.diavolo.gogroceriesapp.domain.model.TripSpending
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Composable
fun AnalyticsRoute(
    onBackClick: () -> Unit,
    onTripClick: (Long) -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AnalyticsScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onTripClick = onTripClick,
        onRetryClick = viewModel::retry
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    uiState: AnalyticsUiState,
    onBackClick: () -> Unit,
    onTripClick: (Long) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Budget & Analytics",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Completed shopping trips",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
        }
    ) { contentPadding ->
        when {
            uiState.isLoading -> LoadingContent(contentPadding)
            uiState.errorMessage != null -> MessageContent(
                contentPadding = contentPadding,
                title = "Couldn't load analytics",
                message = uiState.errorMessage,
                actionLabel = "Try again",
                onActionClick = onRetryClick
            )
            uiState.analytics?.completedTripCount == 0 -> MessageContent(
                contentPadding = contentPadding,
                title = "No completed trips yet",
                message = "Finish a shopping trip to start building spending insights.",
                actionLabel = "Go back",
                onActionClick = onBackClick
            )
            uiState.analytics != null -> AnalyticsContent(
                analytics = uiState.analytics,
                contentPadding = contentPadding,
                onTripClick = onTripClick
            )
        }
    }
}

@Composable
private fun AnalyticsContent(
    analytics: BudgetAnalytics,
    contentPadding: PaddingValues,
    onTripClick: (Long) -> Unit
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
        item(key = "total") {
            TotalSpentCard(analytics)
        }

        item(key = "metrics") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    label = "Average trip",
                    value = analytics.averageSpentPerTrip.toString(),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "Budget adherence",
                    value = if (analytics.fullyPricedBudgetTripCount == 0) {
                        "Not enough data"
                    } else {
                        "${analytics.budgetAdherencePercent}%"
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item(key = "variance") {
            EstimateVarianceCard(analytics)
        }

        if (analytics.missingActualPriceCount > 0) {
            item(key = "missing-prices") {
                MissingPriceCard(analytics.missingActualPriceCount)
            }
        }

        if (analytics.spendingByCategory.isNotEmpty()) {
            item(key = "categories") {
                CategoryBreakdownCard(analytics.spendingByCategory)
            }
        }

        if (analytics.recentTrips.isNotEmpty()) {
            item(key = "trend") {
                SpendingTrendCard(analytics.recentTrips)
            }

            item(key = "recent-header") {
                SectionHeader("Recent completed trips")
            }
            items(analytics.recentTrips.reversed(), key = TripSpending::listId) { trip ->
                RecentTripCard(
                    trip = trip,
                    onClick = { onTripClick(trip.listId) }
                )
            }
        }
    }
}

@Composable
private fun TotalSpentCard(analytics: BudgetAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Outlined.Analytics,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp).size(28.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = "Total actual spending",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = analytics.totalSpent.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Across ${analytics.completedTripCount} completed ${if (analytics.completedTripCount == 1) "trip" else "trips"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
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
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EstimateVarianceCard(analytics: BudgetAnalytics) {
    val variance = analytics.estimateVarianceRupiah
    val positive = variance >= 0

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
                imageVector = if (positive) {
                    Icons.AutoMirrored.Outlined.TrendingDown
                } else {
                    Icons.AutoMirrored.Outlined.TrendingUp
                },
                contentDescription = null
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = if (positive) "Below purchased estimates" else "Above purchased estimates",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${Money(abs(variance))} difference",
                    style = MaterialTheme.typography.bodyMedium
                )
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
                text = "$count purchased ${if (count == 1) "item is" else "items are"} missing actual prices. Totals may be incomplete.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CategoryBreakdownCard(categories: List<CategorySpending>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionHeader("Spending by category")
            Spacer(Modifier.height(16.dp))
            categories.forEachIndexed { index, category ->
                CategorySpendingRow(category)
                if (index != categories.lastIndex) Spacer(Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun CategorySpendingRow(category: CategorySpending) {
    val categoryColor = rememberCategoryColor(category.colorHex)

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(categoryColor, CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = category.categoryName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = category.amount.toString(),
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(Modifier.height(7.dp))
        LinearProgressIndicator(
            progress = { category.sharePercent.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp),
            color = categoryColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun SpendingTrendCard(trips: List<TripSpending>) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val maxAmount = trips.maxOfOrNull { it.amount.rupiah }?.coerceAtLeast(1L) ?: 1L

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionHeader("Recent spending trend")
            Spacer(Modifier.height(16.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val horizontalPadding = 8.dp.toPx()
                val verticalPadding = 10.dp.toPx()
                val chartWidth = size.width - horizontalPadding * 2
                val chartHeight = size.height - verticalPadding * 2

                drawLine(
                    color = gridColor,
                    start = Offset(horizontalPadding, size.height - verticalPadding),
                    end = Offset(size.width - horizontalPadding, size.height - verticalPadding),
                    strokeWidth = 1.dp.toPx()
                )

                val points = trips.mapIndexed { index, trip ->
                    val x = if (trips.size == 1) {
                        size.width / 2
                    } else {
                        horizontalPadding + chartWidth * index / (trips.size - 1)
                    }
                    val normalized = trip.amount.rupiah.toFloat() / maxAmount
                    val y = size.height - verticalPadding - chartHeight * normalized
                    Offset(x, y)
                }

                points.zipWithNext().forEach { (start, end) ->
                    drawLine(
                        color = lineColor,
                        start = start,
                        end = end,
                        strokeWidth = 3.dp.toPx()
                    )
                }
                points.forEach { point ->
                    drawCircle(color = lineColor, radius = 5.dp.toPx(), center = point)
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                trips.firstOrNull()?.let { first ->
                    Text(
                        text = formatDate(first.completedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.weight(1f))
                trips.lastOrNull()?.let { last ->
                    Text(
                        text = formatDate(last.completedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentTripCard(
    trip: TripSpending,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
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
                imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trip.listName,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatDate(trip.completedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = trip.amount.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
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
private fun MessageContent(
    contentPadding: PaddingValues,
    title: String,
    message: String,
    actionLabel: String,
    onActionClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.AccountBalanceWallet,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onActionClick) {
            Text(actionLabel)
        }
    }
}

@Composable
private fun rememberCategoryColor(colorHex: String): Color =
    runCatching { Color(parseColor(colorHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)

private fun formatDate(timestamp: Long): String = Instant
    .ofEpochMilli(timestamp)
    .atZone(ZoneId.systemDefault())
    .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
