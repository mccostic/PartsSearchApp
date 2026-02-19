package com.app.partssearchapp.screens.vehicleselection.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.partssearchapp.screens.vehicleselection.*
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleSelectionView(
  state: VehicleSelectionState,
  onEvent: (VehicleSelectionUIEvent) -> Unit,
  uiEffects: Flow<VehicleSelectionUIEffect>,
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Select Vehicle") },
        navigationIcon = {
          IconButton(onClick = { onEvent(VehicleSelectionUIEvent.BackStep) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
      // Breadcrumb
      if (state.selection.breadcrumb.isNotEmpty()) {
        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "Part Catalog > ${state.selection.breadcrumb}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      // Step indicator
      StepIndicator(currentStep = state.currentStep)

      if (state.isLoading) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator()
        }
      } else {
        when (state.currentStep) {
          SelectionStep.MAKE -> MakeList(
            makes = state.makes,
            searchQuery = state.makeSearchQuery,
            onSearchChanged = { onEvent(VehicleSelectionUIEvent.MakeSearchChanged(it)) },
            onMakeSelected = { onEvent(VehicleSelectionUIEvent.MakeSelected(it)) }
          )
          SelectionStep.MODEL -> ModelList(
            models = state.models,
            makeName = state.selection.make?.name ?: "",
            searchQuery = state.modelSearchQuery,
            onSearchChanged = { onEvent(VehicleSelectionUIEvent.ModelSearchChanged(it)) },
            onModelSelected = { onEvent(VehicleSelectionUIEvent.ModelSelected(it)) }
          )
          SelectionStep.YEAR -> YearList(
            years = state.years,
            modelName = state.selection.model?.name ?: "",
            onYearSelected = { onEvent(VehicleSelectionUIEvent.YearSelected(it)) }
          )
          SelectionStep.ENGINE -> EngineList(
            engines = state.engines,
            onEngineSelected = { onEvent(VehicleSelectionUIEvent.EngineSelected(it)) }
          )
        }
      }
    }
  }
}

@Composable
private fun StepIndicator(currentStep: SelectionStep) {
  val steps = listOf("Make", "Model", "Year", "Engine")
  val currentIndex = currentStep.ordinal

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    steps.forEachIndexed { index, step ->
      val isActive = index <= currentIndex
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.weight(1f)
      ) {
        Surface(
          shape = MaterialTheme.shapes.small,
          color = if (isActive) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.size(32.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(
              text = "${index + 1}",
              color = if (isActive) MaterialTheme.colorScheme.onPrimary
                      else MaterialTheme.colorScheme.onSurfaceVariant,
              style = MaterialTheme.typography.labelMedium,
            )
          }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = step,
          style = MaterialTheme.typography.labelSmall,
          color = if (isActive) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
  HorizontalDivider()
}

@Composable
private fun MakeList(
  makes: List<com.app.partssearchapp.data.models.VehicleMake>,
  searchQuery: String,
  onSearchChanged: (String) -> Unit,
  onMakeSelected: (com.app.partssearchapp.data.models.VehicleMake) -> Unit,
) {
  val filteredMakes = remember(makes, searchQuery) {
    if (searchQuery.isBlank()) makes
    else makes.filter { it.name.contains(searchQuery, ignoreCase = true) }
  }

  val listState = rememberLazyListState()

  LazyColumn(state = listState) {
    item {
      Text(
        text = "Select Vehicle Make",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
      )
    }
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChanged,
        placeholder = { Text("Search makes...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { onSearchChanged("") }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear")
            }
          }
        },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
      )
    }
    if (filteredMakes.isEmpty() && searchQuery.isNotBlank()) {
      item {
        Box(
          modifier = Modifier.fillMaxWidth().padding(32.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "No makes found for \"$searchQuery\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
    items(filteredMakes) { make ->
      ListItem(
        headlineContent = { Text(make.name) },
        leadingContent = {
          Icon(Icons.Default.DirectionsCar, contentDescription = null)
        },
        trailingContent = {
          Icon(Icons.Default.ChevronRight, contentDescription = null)
        },
        modifier = Modifier.clickable { onMakeSelected(make) }
      )
      HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }
  }
}

@Composable
private fun ModelList(
  models: List<com.app.partssearchapp.data.models.VehicleModel>,
  makeName: String,
  searchQuery: String,
  onSearchChanged: (String) -> Unit,
  onModelSelected: (com.app.partssearchapp.data.models.VehicleModel) -> Unit,
) {
  val filteredModels = remember(models, searchQuery) {
    if (searchQuery.isBlank()) models
    else models.filter { it.name.contains(searchQuery, ignoreCase = true) }
  }

  val listState = rememberLazyListState()

  LazyColumn(state = listState) {
    item {
      Text(
        text = "Select Model for $makeName",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
      )
    }
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChanged,
        placeholder = { Text("Search models...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { onSearchChanged("") }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear")
            }
          }
        },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
      )
    }
    if (filteredModels.isEmpty() && searchQuery.isNotBlank()) {
      item {
        Box(
          modifier = Modifier.fillMaxWidth().padding(32.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "No models found for \"$searchQuery\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    } else if (filteredModels.isEmpty()) {
      item {
        Box(
          modifier = Modifier.fillMaxWidth().padding(32.dp),
          contentAlignment = Alignment.Center,
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              Icons.Default.SearchOff,
              contentDescription = null,
              modifier = Modifier.size(48.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "No models found for $makeName",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }
    items(filteredModels) { model ->
      ListItem(
        headlineContent = { Text(model.name) },
        leadingContent = {
          Icon(Icons.Default.DirectionsCar, contentDescription = null)
        },
        trailingContent = {
          Icon(Icons.Default.ChevronRight, contentDescription = null)
        },
        modifier = Modifier.clickable { onModelSelected(model) }
      )
      HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }
  }
}

@Composable
private fun YearList(
  years: List<Int>,
  modelName: String,
  onYearSelected: (Int) -> Unit,
) {
  LazyColumn {
    item {
      Text(
        text = "Select Year for $modelName",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(16.dp),
      )
    }
    items(years) { year ->
      ListItem(
        headlineContent = { Text(year.toString()) },
        leadingContent = {
          Icon(Icons.Default.CalendarMonth, contentDescription = null)
        },
        trailingContent = {
          Icon(Icons.Default.ChevronRight, contentDescription = null)
        },
        modifier = Modifier.clickable { onYearSelected(year) }
      )
      HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }
  }
}

@Composable
private fun EngineList(
  engines: List<com.app.partssearchapp.data.models.VehicleEngine>,
  onEngineSelected: (com.app.partssearchapp.data.models.VehicleEngine) -> Unit,
) {
  LazyColumn {
    item {
      Text(
        text = "Select Engine",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(16.dp),
      )
    }
    items(engines) { engine ->
      ListItem(
        headlineContent = { Text(engine.description) },
        leadingContent = {
          Icon(Icons.Default.Settings, contentDescription = null)
        },
        trailingContent = {
          Icon(Icons.Default.ChevronRight, contentDescription = null)
        },
        modifier = Modifier.clickable { onEngineSelected(engine) }
      )
      HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }
  }
}
