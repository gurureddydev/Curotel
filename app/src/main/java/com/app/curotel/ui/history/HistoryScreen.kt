package com.app.curotel.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.curotel.core.ui.GlassCard
import com.app.curotel.ui.theme.*

data class HistoryItem(
    val id: Int,
    val type: String,
    val value: String,
    val unit: String,
    val timestamp: String,
    val color: androidx.compose.ui.graphics.Color
)

@Composable
fun HistoryScreen() {
    // Sample history data
    val historyItems = remember {
        listOf(
            HistoryItem(1, "Temperature", "36.8", "°C", "Today, 2:30 PM", NeonCyan),
            HistoryItem(2, "SpO2", "98", "%", "Today, 2:25 PM", NeonRose),
            HistoryItem(3, "Heart Rate", "72", "BPM", "Today, 2:25 PM", NeonRose),
            HistoryItem(4, "Blood Pressure", "120/80", "mmHg", "Today, 2:20 PM", NeonPurple),
            HistoryItem(5, "Temperature", "36.6", "°C", "Yesterday, 10:15 AM", NeonCyan),
            HistoryItem(6, "SpO2", "97", "%", "Yesterday, 10:10 AM", NeonRose),
            HistoryItem(7, "Blood Pressure", "118/78", "mmHg", "Yesterday, 10:05 AM", NeonPurple),
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Header
        Text(
            text = "MEASUREMENT HISTORY",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = NeonCyan
        )
        
        Text(
            text = "View your past health readings",
            style = MaterialTheme.typography.bodySmall,
            color = TextGrey
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipItem("All", true, NeonCyan)
            FilterChipItem("Today", false, NeonLime)
            FilterChipItem("Week", false, NeonPurple)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // History list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(historyItems) { item ->
                HistoryCard(item)
            }
        }
    }
}

@Composable
fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    color: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) color.copy(alpha = 0.15f) else GlassSurface.copy(alpha = 0.3f),
        modifier = Modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) color else TextGrey,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun HistoryCard(item: HistoryItem) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = item.type.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextGrey
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = item.value,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = item.color
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = item.color.copy(alpha = 0.7f)
                    )
                }
            }
            
            Text(
                text = item.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = TextGrey
            )
        }
    }
}
