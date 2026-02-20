package com.app.partssearchapp.screens.profile.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.partssearchapp.screens.profile.ProfileState
import com.app.partssearchapp.screens.profile.ProfileTab
import com.app.partssearchapp.screens.profile.ProfileUIEffect
import com.app.partssearchapp.screens.profile.ProfileUIEvent
import com.app.partssearchapp.screens.profile.UserProfile
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileView(
    state: ProfileState,
    onEvent: (ProfileUIEvent) -> Unit,
    uiEffects: Flow<ProfileUIEffect>,
    modifier: Modifier = Modifier,
) {
    // Handle UI Effects (global snackbar)
    LaunchedEffect(Unit) {
        uiEffects.collect { effect ->
            when (effect) {
                is ProfileUIEffect.ShowConfirmationDialog -> { /* future */ }
            }
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = modifier.fillMaxSize().padding(paddingValues)
        ) {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(ProfileUIEvent.BackPressed) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.uiState.isEditing) {
                        TextButton(onClick = { onEvent(ProfileUIEvent.CancelEditing) }) { Text("Cancel") }
                        TextButton(
                            onClick = { onEvent(ProfileUIEvent.SaveProfile) },
                            enabled = !state.processState.isSaving
                        ) {
                            if (state.processState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Save")
                            }
                        }
                    } else {
                        IconButton(onClick = {
                            onEvent(ProfileUIEvent.RefreshProfile)
                        }) { Icon(Icons.Outlined.Refresh, contentDescription = "Refresh") }
                        IconButton(onClick = {
                            onEvent(ProfileUIEvent.StartEditing)
                        }) { Icon(Icons.Outlined.Edit, contentDescription = "Edit") }
                        IconButton(onClick = {
                            onEvent(ProfileUIEvent.LogoutClicked)
                        }) { Icon(Icons.AutoMirrored.Outlined.ExitToApp, contentDescription = "Logout") }
                    }
                }
            )

            if (state.processState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileHeader(
                        userProfile = state.userProfile,
                        isEditing = state.uiState.isEditing,
                        onNameChange = { onEvent(ProfileUIEvent.UpdateName(it)) },
                        onBioChange = { onEvent(ProfileUIEvent.UpdateBio(it)) }
                    )
                    StatsCard(userProfile = state.userProfile)
                    ProfileTabs(
                        selectedTab = state.uiState.selectedTab,
                        onTabSelected = { onEvent(ProfileUIEvent.TabSelected(it)) }
                    )
                    when (state.uiState.selectedTab) {
                        ProfileTab.SETTINGS -> SettingsContent(
                            onDeleteAccount = { onEvent(ProfileUIEvent.ShowDeleteDialog) },
                            onTriggerError = { onEvent(ProfileUIEvent.TriggerError) }
                        )
                        ProfileTab.ACTIVITY -> ActivityContent(userProfile = state.userProfile)
                        ProfileTab.PREFERENCES -> PreferencesContent()
                    }
                }
            }

            if (state.uiState.showDeleteDialog) {
                DeleteAccountDialog(
                    isDeleting = state.processState.isDeleting,
                    onConfirm = { onEvent(ProfileUIEvent.DeleteAccount) },
                    onDismiss = { onEvent(ProfileUIEvent.DismissDeleteDialog) }
                )
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    userProfile: UserProfile,
    isEditing: Boolean,
    onNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userProfile.name.take(2).uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Name
            if (isEditing) {
                OutlinedTextField(
                    value = userProfile.name,
                    onValueChange = onNameChange,
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = userProfile.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Email
            Text(
                text = userProfile.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Bio
            if (isEditing) {
                OutlinedTextField(
                    value = userProfile.bio,
                    onValueChange = onBioChange,
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            } else {
                Text(
                    text = userProfile.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Join Date
            Text(
                text = "Joined on ${userProfile.joinDate}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatsCard(userProfile: UserProfile, modifier: Modifier = Modifier,) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = Icons.AutoMirrored.Outlined.Login,
                label = "Total Logins",
                value = userProfile.totalLogins.toString()
            )
            StatItem(
                icon = Icons.Outlined.Schedule,
                label = "Last Login",
                value = userProfile.lastLoginTime
            )
        }
    }
}

@Composable
private fun StatItem(icon: ImageVector, label: String, value: String,) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileTabs(selectedTab: ProfileTab, onTabSelected: (ProfileTab) -> Unit, modifier: Modifier = Modifier,) {
    TabRow(
        selectedTabIndex = selectedTab.ordinal,
        modifier = modifier.fillMaxWidth()
    ) {
        ProfileTab.entries.forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = { Text(tab.name) }
            )
        }
    }
}

@Composable
private fun SettingsContent(onDeleteAccount: () -> Unit, onTriggerError: () -> Unit, modifier: Modifier = Modifier,) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingItem(
            icon = Icons.Outlined.Notifications,
            title = "Notifications",
            subtitle = "Manage your notification preferences"
        )
        SettingItem(
            icon = Icons.Outlined.Security,
            title = "Privacy & Security",
            subtitle = "Control your privacy settings"
        )
        SettingItem(
            icon = Icons.Outlined.Language,
            title = "Language",
            subtitle = "Choose your preferred language"
        )

        HorizontalDivider(Modifier.padding(vertical = 8.dp))

        // Trigger Error Button for testing listener
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            onClick = onTriggerError
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Test Error (Listener Demo)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            onClick = onDeleteAccount
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Delete Account",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun ActivityContent(userProfile: UserProfile, modifier: Modifier = Modifier,) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActivityItem(
            icon = Icons.AutoMirrored.Outlined.Login,
            title = "Last Login",
            subtitle = "Today at ${userProfile.lastLoginTime}",
            time = "Now"
        )
        ActivityItem(
            icon = Icons.Outlined.Edit,
            title = "Profile Updated",
            subtitle = "You updated your profile information",
            time = "2h ago"
        )
        ActivityItem(
            icon = Icons.Outlined.Person,
            title = "Account Created",
            subtitle = "Welcome to the app!",
            time = userProfile.joinDate
        )
    }
}

@Composable
private fun PreferencesContent(modifier: Modifier = Modifier,) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PreferenceItem(
            icon = Icons.Outlined.DarkMode,
            title = "Dark Mode",
            subtitle = "Use dark theme",
            isToggled = true
        )
        PreferenceItem(
            icon = Icons.Outlined.Notifications,
            title = "Push Notifications",
            subtitle = "Receive notifications",
            isToggled = true
        )
        PreferenceItem(
            icon = Icons.Outlined.LocationOn,
            title = "Location Services",
            subtitle = "Allow location access",
            isToggled = false
        )
    }
}

@Composable
private fun SettingItem(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier,) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActivityItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    time: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PreferenceItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isToggled: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isToggled,
                onCheckedChange = { /* Handle toggle */ }
            )
        }
    }
}

@Composable
private fun DeleteAccountDialog(isDeleting: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit,) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Account") },
        text = { Text("Are you sure you want to delete your account? This action cannot be undone.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isDeleting
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Delete")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
