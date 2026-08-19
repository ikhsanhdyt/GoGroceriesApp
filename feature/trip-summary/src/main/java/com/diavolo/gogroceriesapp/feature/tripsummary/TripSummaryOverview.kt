package com.diavolo.gogroceriesapp.feature.tripsummary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.diavolo.gogroceriesapp.domain.Money
import com.diavolo.gogroceriesapp.domain.model.TripSummary
import kotlin.math.abs

@Composable
internal fun ActualSpentCard(summary: TripSummary) {
    val estimateDifference = summary.actualSpent.rupiah - summary.estimatedPurchasedTotal.rupiah

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
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
                    estimateDifference > 0 -> "${Money(estimateDifference)} above the purchased estimate"
                    estimateDifference < 0 -> "${Money(abs(estimateDifference))} below the purchased estimate"
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
internal fun SummaryMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun BudgetResultCard(summary: TripSummary) {
    val remaining = summary.budgetRemainingRupiah
    val (title, message, positive) = when {
        remaining == null -> Triple("No budget comparison", "This trip did not have a budget.", true)
        remaining > 0 -> Triple("Under budget", "${Money(remaining)} remaining", true)
        remaining < 0 -> Triple("Over budget", "${Money(abs(remaining))} over", false)
        else -> Triple("On budget", "Spent exactly the planned budget", true)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (positive) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(message, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
internal fun MissingPriceCard(count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Info, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(
                text = "$count purchased ${if (count == 1) "item has" else "items have"} no actual price, so actual spending may be incomplete.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
