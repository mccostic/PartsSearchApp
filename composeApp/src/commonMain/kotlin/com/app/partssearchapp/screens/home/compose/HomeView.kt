package com.app.partssearchapp.screens.home.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.partssearchapp.screens.home.HomeState
import com.app.partssearchapp.screens.home.HomeUIEffect
import com.app.partssearchapp.screens.home.HomeUIEvent
import com.app.partssearchapp.screens.home.UserInfo
import com.app.partssearchapp.screens.home.data.Repository
import kotlinx.coroutines.flow.Flow

/**
 * Home screen UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(
  state: HomeState,
  onEvent: (HomeUIEvent) -> Unit,
  uiEffects: Flow<HomeUIEffect>,
) {
  // Handle UI Effects
  LaunchedEffect(Unit) {
    uiEffects.collect { effect ->
      when (effect) {
        is HomeUIEffect.ShowWelcomeDialog -> {
          // Handle welcome dialog if needed
        }
      }
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Welcome ${state.userInfo.name}") },
        actions = {
          IconButton(onClick = { onEvent(HomeUIEvent.NavigateToProfile) }) {
            Icon(Icons.Default.Person, contentDescription = "Profile")
          }
          IconButton(onClick = { onEvent(HomeUIEvent.LogoutClicked) }) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
          }
        }
      )
    },
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = { onEvent(HomeUIEvent.RefreshData) },
        icon = { 
          if (state.processState.refreshing) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp))
          } else {
            Icon(Icons.Default.Refresh, contentDescription = null)
          }
        },
        text = { Text("Refresh") }
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        UserInfoCard(
          userInfo = state.userInfo,
            onShowWelcome = { onEvent(HomeUIEvent.ShowWelcomeDialog) }
        )
      }
      
      // Repositories Section
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "My Repositories",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
          )
          if (state.processState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp))
          } else {
            IconButton(onClick = { onEvent(HomeUIEvent.LoadRepositories) }) {
              Icon(Icons.Default.Refresh, contentDescription = "Refresh Repositories")
            }
          }
        }
      }
      
      if (state.repositories.isNotEmpty()) {
        items(state.repositories) { repository ->
          RepositoryCard(
            repository = repository,
            onClick = {
              onEvent(HomeUIEvent.RepositoryClicked(repository))
            }
          )
        }
      } else if (!state.processState.isLoading) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "No repositories found",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(8.dp))
              TextButton(onClick = { onEvent(HomeUIEvent.LoadRepositories) }) {
                Text("Load Repositories")
              }
            }
          }
        }
      }
      
      item {
        Text(
          text = "Features",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold
        )
      }
      items(getDemoFeatures()) { feature ->
        FeatureCard(
          feature = feature,
          onClick = { onEvent(HomeUIEvent.TabSelected(feature.id)) }
        )
      }
    }
  }

  // Welcome Dialog
  if (state.uiState.showWelcomeDialog) {
    WelcomeDialog(
      userName = state.userInfo.name,
      isNewUser = state.userInfo.loginType == "sign_up",
      onDismiss = { onEvent(HomeUIEvent.DismissWelcomeDialog) }
    )
  }
}

@Composable
private fun UserInfoCard(
  userInfo: UserInfo,
  onShowWelcome: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
  ) {
    Column(
      modifier = Modifier.padding(16.dp)
    ) {
      Text(
        text = "User Information",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(8.dp))
      InfoRow(label = "Email", value = userInfo.email)
      InfoRow(label = "Name", value = userInfo.name)
      InfoRow(label = "Login Type", value = userInfo.loginType.replace("_", " ").uppercase())
      InfoRow(label = "Login Time", value = userInfo.loginTime)
      Spacer(modifier = Modifier.height(8.dp))
      TextButton(onClick = onShowWelcome) {
        Text("Show Welcome Message")
      }
    }
  }
}

@Composable
private fun InfoRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = "$label:",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Medium
    )
  }
}

@Composable
private fun RepositoryCard(
  repository: Repository,
  onClick: () -> Unit
) {
  Card(
    onClick = onClick,
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = repository.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = repository.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        
        Icon(
          imageVector = if (repository.isPrivate) Icons.Default.Lock else Icons.Default.Public,
          contentDescription = if (repository.isPrivate) "Private" else "Public",
          tint = if (repository.isPrivate) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp)
        )
      }
      
      Spacer(modifier = Modifier.height(8.dp))
      
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Code,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = repository.language,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = repository.stars.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
private fun FeatureCard(
  feature: DemoFeature,
  onClick: () -> Unit
) {
  Card(
    onClick = onClick,
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = feature.icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary
      )
      Spacer(modifier = Modifier.width(16.dp))
      Column {
        Text(
          text = feature.title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Medium
        )
        Text(
          text = feature.description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@Composable
private fun WelcomeDialog(
  userName: String,
  isNewUser: Boolean,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { 
      Text(if (isNewUser) "Welcome to the App!" else "Welcome Back!") 
    },
    text = { 
      Text(
        if (isNewUser) 
          "Hi $userName! Thanks for signing up. We're excited to have you on board!"
        else 
          "Welcome back, $userName! Great to see you again."
      ) 
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Got it!")
      }
    }
  )
}

// Demo data
data class DemoFeature(
  val id: Int,
  val title: String,
  val description: String,
  val icon: ImageVector
)

private fun getDemoFeatures() = listOf(
  DemoFeature(
    id = 0,
    title = "Dashboard",
    description = "View your main dashboard with quick stats",
    icon = Icons.Default.Dashboard
  ),
  DemoFeature(
    id = 1,
    title = "Analytics",
    description = "Check detailed analytics and reports",
    icon = Icons.Default.Analytics
  ),
  DemoFeature(
    id = 2,
    title = "Settings",
    description = "Manage your app preferences and settings",
    icon = Icons.Default.Settings
  ),
  DemoFeature(
    id = 3,
    title = "Help & Support",
    description = "Get help and contact our support team",
    icon = Icons.AutoMirrored.Filled.Help
  ),
)
