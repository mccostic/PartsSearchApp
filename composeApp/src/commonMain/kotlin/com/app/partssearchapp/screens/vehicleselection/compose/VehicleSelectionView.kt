package com.app.partssearchapp.screens.vehicleselection.compose

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
            onMakeSelected = { onEvent(VehicleSelectionUIEvent.MakeSelected(it)) }
          )
          SelectionStep.YEAR -> YearList(
            years = state.years,
            makeName = state.selection.make?.name ?: "",
            onYearSelected = { onEvent(VehicleSelectionUIEvent.YearSelected(it)) }
          )
          SelectionStep.MODEL -> ModelList(
            models = state.models,
            onModelSelected = { onEvent(VehicleSelectionUIEvent.ModelSelected(it)) }
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
  val steps = listOf("Make", "Year", "Model", "Engine")
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
  onMakeSelected: (com.app.partssearchapp.data.models.VehicleMake) -> Unit,
) {
  LazyColumn {
    item {
      Text(
        text = "Select Vehicle Make",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(16.dp),
      )
    }
    items(makes) { make ->
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
private fun YearList(
  years: List<Int>,
  makeName: String,
  onYearSelected: (Int) -> Unit,
) {
  LazyColumn {
    item {
      Text(
        text = "Select Year for $makeName",
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
private fun ModelList(
  models: List<com.app.partssearchapp.data.models.VehicleModel>,
  onModelSelected: (com.app.partssearchapp.data.models.VehicleModel) -> Unit,
) {
  LazyColumn {
    item {
      Text(
        text = "Select Model",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(16.dp),
      )
    }
    items(models) { model ->
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
