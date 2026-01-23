package com.app.curotel.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.curotel.core.ui.GlassCard
import com.app.curotel.ui.theme.*

@Composable
fun SettingsScreen() {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoConnect by remember { mutableStateOf(true) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Header
        Text(
            text = "SETTINGS",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = NeonCyan
        )
        
        Text(
            text = "Manage your app preferences",
            style = MaterialTheme.typography.bodySmall,
            color = TextGrey
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Profile Section
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.2f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👤",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "John Doe",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )
                    Text(
                        text = "john.doe@email.com",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGrey
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Device Settings
        Text(
            text = "DEVICE",
            style = MaterialTheme.typography.labelMedium,
            color = TextGrey
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        SettingsItem(
            icon = "📱",
            title = "CURO X1",
            subtitle = "Connected",
            trailing = {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(NeonLime, CircleShape)
                )
            }
        )
        
        SettingsToggleItem(
            icon = "🔗",
            title = "Auto Connect",
            subtitle = "Automatically connect to device",
            checked = autoConnect,
            onCheckedChange = { autoConnect = it }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Notifications
        Text(
            text = "NOTIFICATIONS",
            style = MaterialTheme.typography.labelMedium,
            color = TextGrey
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        SettingsToggleItem(
            icon = "🔔",
            title = "Push Notifications",
            subtitle = "Receive health alerts",
            checked = notificationsEnabled,
            onCheckedChange = { notificationsEnabled = it }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // About
        Text(
            text = "ABOUT",
            style = MaterialTheme.typography.labelMedium,
            color = TextGrey
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        SettingsItem(
            icon = "ℹ️",
            title = "About Curotel",
            subtitle = "Version 1.0.0",
            trailing = {}
        )
        
        SettingsItem(
            icon = "📋",
            title = "Privacy Policy",
            subtitle = "View our privacy policy",
            trailing = {}
        )
        
        SettingsItem(
            icon = "❓",
            title = "Help & Support",
            subtitle = "Get help with the app",
            trailing = {}
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // App version
        Text(
            text = "Curotel v1.0.0 • Built with ❤️",
            style = MaterialTheme.typography.labelSmall,
            color = TextGrey,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun SettingsItem(
    icon: String,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = TextWhite
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGrey
                )
            }
            
            trailing()
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = TextWhite
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGrey
                )
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NeonCyan,
                    checkedTrackColor = NeonCyan.copy(alpha = 0.3f),
                    uncheckedThumbColor = TextGrey,
                    uncheckedTrackColor = GlassSurface
                )
            )
        }
    }
}
