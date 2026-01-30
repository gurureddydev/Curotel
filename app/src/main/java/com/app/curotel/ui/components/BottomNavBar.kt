package com.app.curotel.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.curotel.navigation.Screen
import com.app.curotel.ui.theme.*

data class BottomNavItem(
    val screen: Screen,
    val icon: String,
    val label: String
)

@Composable
fun CurotelBottomNavBar(
    currentRoute: String,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(Screen.Home, "🏠", "Home"),
        BottomNavItem(Screen.History, "📊", "History"),
        BottomNavItem(Screen.Consult, "📹", "Consult"),
        BottomNavItem(Screen.Chat, "💬", "Chat"),
        BottomNavItem(Screen.Settings, "⚙️", "Settings")
    )
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        color = DeepSpaceBlack,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                BottomNavItemView(
                    item = item,
                    isSelected = currentRoute == item.screen.route,
                    onClick = { onNavigate(item.screen) }
                )
            }
        }
    }
}

@Composable
fun BottomNavItemView(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) NeonCyan.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "navItemBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) NeonCyan else TextGrey,
        animationSpec = tween(durationMillis = 200),
        label = "navItemContent"
    )
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        modifier = Modifier
            .height(56.dp)
            .widthIn(min = 64.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.icon,
                style = MaterialTheme.typography.titleMedium
            )
            
            if (isSelected) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = contentColor
                )
            }
        }
    }
}
