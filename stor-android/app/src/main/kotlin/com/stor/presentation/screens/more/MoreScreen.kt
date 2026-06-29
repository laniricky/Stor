package com.stor.presentation.screens.more

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stor.presentation.theme.*

@Composable
fun MoreScreen(
    viewModel: MoreViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit = {},
    onLogoutComplete: () -> Unit = {}
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.logoutComplete.collect {
            onLogoutComplete()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.syncState.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(TealPrimary.copy(alpha = 0.15f), Color.Transparent)))
                    .padding(horizontal = 20.dp, vertical = 28.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(64.dp).clip(CircleShape)
                            .background(Brush.linearGradient(listOf(TealPrimary, LimeSecondary))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(userProfile.initials, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = BackgroundDark)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(userProfile.name, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground)
                        Text(userProfile.email, fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            SettingsSection("Finance") {
                SettingsItem(Icons.Default.List, "Manage Categories", TealPrimary) {
                    // Navigate to categories (low priority)
                }
                SettingsItem(Icons.Default.Savings, "Budget Settings", TealPrimary) {
                    onNavigate("budget_settings")
                }
            }

            SettingsSection("Notifications") {
                SettingsItem(Icons.Default.Notifications, "Loan Reminders", LoanColor) {
                    onNavigate("notification_settings")
                }
                SettingsItem(Icons.Default.NotificationImportant, "Budget Alerts", ExpenseColor) {
                    onNavigate("notification_settings")
                }
            }

            SettingsSection("Data") {
                SettingsItem(Icons.Default.Sync, "Sync Now", IncomeColor) {
                    viewModel.syncAll()
                }
                SettingsItem(Icons.Default.Save, "Backup Data", TealPrimary) {
                    // Backup logic (TODO)
                }
                SettingsItem(Icons.Default.Download, "Export Reports", TealPrimary) {
                    onNavigate("export_reports")
                }
            }

            SettingsSection("Account") {
                SettingsItem(Icons.Default.Person, "Edit Profile", TealPrimary) {
                    onNavigate("edit_profile")
                }
                SettingsItem(Icons.Default.Lock, "Change Password", TealPrimary) {
                    onNavigate("change_password")
                }
                SettingsItem(Icons.Default.ExitToApp, "Sign Out", ExpenseColor) {
                    viewModel.logout()
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Stor v1.0.0",
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column { content() }
    }
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun SettingsItem(icon: ImageVector, label: String, iconColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
    }
}
