package com.app.partssearchapp.screens.categories.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.partssearchapp.data.models.PartCategory
import com.app.partssearchapp.screens.categories.*
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesView(
  state: CategoriesState,
  onEvent: (CategoriesUIEvent) -> Unit,
  uiEffects: Flow<CategoriesUIEffect>,
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Part Categories") },
        navigationIcon = {
          IconButton(onClick = { onEvent(CategoriesUIEvent.BackPressed) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = { onEvent(CategoriesUIEvent.GoToCart) }) {
            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
          }
        }
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Vehicle breadcrumb
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Part Catalog > ${state.vehicleBreadcrumb}",
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      if (state.isLoading) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator()
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize()
        ) {
          item {
            Text(
              text = "Select Part Category",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(16.dp),
            )
          }

          items(state.categories) { category ->
            CategoryItem(
              category = category,
              isExpanded = state.expandedCategoryId == category.id,
              onCategoryClick = { onEvent(CategoriesUIEvent.CategoryClicked(category)) },
              onSubcategoryClick = { onEvent(CategoriesUIEvent.SubcategoryClicked(it)) },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun CategoryItem(
  category: PartCategory,
  isExpanded: Boolean,
  onCategoryClick: () -> Unit,
  onSubcategoryClick: (PartCategory) -> Unit,
) {
  val icon = getCategoryIcon(category.name)

  Column {
    ListItem(
      headlineContent = {
        Text(
          text = category.name,
          fontWeight = FontWeight.Medium,
        )
      },
      leadingContent = {
        Icon(
          icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
        )
      },
      trailingContent = {
        if (category.subcategories.isNotEmpty()) {
          Icon(
            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
          )
        } else {
          Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
      },
      modifier = Modifier.clickable { onCategoryClick() }
    )

    AnimatedVisibility(visible = isExpanded && category.subcategories.isNotEmpty()) {
      Column {
        category.subcategories.forEach { subcategory ->
          ListItem(
            headlineContent = {
              Text(
                text = subcategory.name,
                style = MaterialTheme.typography.bodyMedium,
              )
            },
            leadingContent = {
              Spacer(modifier = Modifier.width(24.dp))
            },
            trailingContent = {
              Icon(Icons.Default.ChevronRight, contentDescription = null)
            },
            modifier = Modifier
              .padding(start = 24.dp)
              .clickable { onSubcategoryClick(subcategory) }
          )
          HorizontalDivider(modifier = Modifier.padding(start = 64.dp))
        }
      }
    }

    HorizontalDivider()
  }
}

private fun getCategoryIcon(categoryName: String): androidx.compose.ui.graphics.vector.ImageVector {
  return when {
    categoryName.contains("Brake", true) -> Icons.Default.Circle
    categoryName.contains("Engine", true) -> Icons.Default.Settings
    categoryName.contains("Cooling", true) -> Icons.Default.AcUnit
    categoryName.contains("Fuel", true) -> Icons.Default.LocalGasStation
    categoryName.contains("Electrical", true) -> Icons.Default.ElectricalServices
    categoryName.contains("Suspension", true) -> Icons.Default.Height
    categoryName.contains("Transmission", true) -> Icons.Default.Tune
    categoryName.contains("Exhaust", true) -> Icons.Default.Air
    categoryName.contains("Body", true) -> Icons.Default.DirectionsCar
    else -> Icons.Default.Build
  }
}
