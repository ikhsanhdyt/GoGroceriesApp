package com.diavolo.gogroceriesapp.feature.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diavolo.gogroceriesapp.core.ui.LoadingStateContent
import com.diavolo.gogroceriesapp.core.ui.MessageStateContent
import com.diavolo.gogroceriesapp.core.ui.categoryColor
import com.diavolo.gogroceriesapp.domain.model.Category

@Composable
fun CategoriesRoute(
    onBackClick: () -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var editorTarget by remember { mutableStateOf<EditorTarget?>(null) }
    var pendingDelete by remember { mutableStateOf<Category?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                CategoriesEvent.CategorySaved -> editorTarget = null
                CategoriesEvent.CategoryDeleted -> pendingDelete = null
            }
        }
    }

    CategoriesScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onRetryClick = viewModel::retryLoading,
        onAddClick = {
            viewModel.clearActionError()
            editorTarget = EditorTarget.New
        },
        onEditClick = { category ->
            viewModel.clearActionError()
            editorTarget = EditorTarget.Edit(category)
        },
        onDeleteClick = { category ->
            viewModel.clearActionError()
            pendingDelete = category
        }
    )

    editorTarget?.let { target ->
        val editing = (target as? EditorTarget.Edit)?.category
        CategoryFormSheet(
            category = editing,
            isSaving = uiState.isSaving,
            errorMessage = uiState.actionError,
            onDismiss = { editorTarget = null },
            onSave = { name, colorHex ->
                if (editing == null) {
                    viewModel.addCategory(name, colorHex)
                } else {
                    viewModel.updateCategory(editing.copy(name = name, colorHex = colorHex))
                }
            }
        )
    }

    pendingDelete?.let { category ->
        DeleteCategoryDialog(
            category = category,
            isDeleting = uiState.isDeleting,
            errorMessage = uiState.actionError,
            onDismiss = { pendingDelete = null },
            onConfirm = { viewModel.deleteCategory(category.id) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    uiState: CategoriesUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (Category) -> Unit,
    onDeleteClick: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Categories",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Group items the way you shop",
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
        },
        floatingActionButton = {
            if (!uiState.isLoading && uiState.loadError == null) {
                ExtendedFloatingActionButton(
                    onClick = onAddClick,
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    text = { Text("New category") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { contentPadding ->
        when {
            uiState.isLoading -> LoadingStateContent(contentPadding)
            uiState.loadError != null -> MessageStateContent(
                contentPadding = contentPadding,
                title = "We couldn't load your categories",
                message = uiState.loadError,
                actionLabel = "Try again",
                onActionClick = onRetryClick
            )
            uiState.categories.isEmpty() -> MessageStateContent(
                contentPadding = contentPadding,
                title = "No categories yet",
                message = "Add a category to group your items by aisle.",
                actionLabel = "Add a category",
                onActionClick = onAddClick
            )
            else -> CategoryList(
                categories = uiState.categories,
                contentPadding = contentPadding,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick
            )
        }
    }
}

@Composable
private fun CategoryList(
    categories: List<Category>,
    contentPadding: PaddingValues,
    onEditClick: (Category) -> Unit,
    onDeleteClick: (Category) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            end = 20.dp,
            bottom = contentPadding.calculateBottomPadding() + 88.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(categories, key = Category::id) { category ->
            CategoryRow(
                category = category,
                onEditClick = { onEditClick(category) },
                onDeleteClick = { onDeleteClick(category) }
            )
        }
    }
}

@Composable
private fun CategoryRow(
    category: Category,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        onClick = onEditClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 18.dp, top = 6.dp, bottom = 6.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(categoryColor(category.colorHex), CircleShape)
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit ${category.name}"
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete ${category.name}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private sealed interface EditorTarget {
    data object New : EditorTarget
    data class Edit(val category: Category) : EditorTarget
}
