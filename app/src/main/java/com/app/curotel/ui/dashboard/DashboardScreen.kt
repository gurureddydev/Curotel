package com.app.curotel.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.curotel.core.ui.SectionHeader
import com.app.curotel.core.ui.PulsingDot
import com.app.curotel.core.ui.scaleOnPress
import com.app.curotel.core.ui.slideUpOnAppear
import com.app.curotel.core.ui.pulseGlow
import com.app.curotel.domain.model.DeviceConnectionState
import com.app.curotel.domain.model.MeasurementType
import com.app.curotel.domain.model.VitalsPacket
import com.app.curotel.ui.bpmonitor.BpMonitorScreen
import com.app.curotel.ui.otoscope.OtoscopeScreen
import com.app.curotel.ui.oximeter.OximeterScreen
import com.app.curotel.ui.theme.*
import com.app.curotel.ui.thermometer.ThermometerScreen
import com.app.curotel.ui.stethoscope.StethoscopeScreen
import com.app.curotel.viewmodel.DeviceViewModel
import kotlin.math.sin

@Composable
fun DashboardScreen(viewModel: DeviceViewModel) {
    val connectionState by viewModel.connectionState.collectAsState()
    val activeMeasurement by viewModel.activeMeasurement.collectAsState()
    val vitals by viewModel.currentVitals.collectAsState()

    var selectedTab by remember { mutableStateOf(MeasurementType.NONE) }
    
    LaunchedEffect(activeMeasurement) {
        if (activeMeasurement != MeasurementType.NONE) {
            selectedTab = activeMeasurement
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(20.dp)
        ) {
            
            // Header
            HeaderBar(
                connectionState = connectionState,
                onConnectToggle = { 
                    if (connectionState == DeviceConnectionState.CONNECTED) viewModel.disconnectDevice() else viewModel.connectDevice() 
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Content
            Box(modifier = Modifier.weight(1f)) {
                if (connectionState != DeviceConnectionState.CONNECTED) {
                    DisconnectPlaceholder(onConnect = { viewModel.connectDevice() })
                } else if (selectedTab == MeasurementType.NONE) {
                    DashboardGrid(onSelect = { selectedTab = it })
                } else {
                    MeasurementContainer(
                        type = selectedTab,
                        vitals = vitals,
                        activeMeasurement = activeMeasurement,
                        onBack = { selectedTab = MeasurementType.NONE },
                        onStart = { viewModel.startMeasurement(selectedTab) },
                        onStop = { viewModel.stopMeasurement() }
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderBar(
    connectionState: DeviceConnectionState,
    onConnectToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "PATIENT MONITORING",
                style = MaterialTheme.typography.labelSmall,
                color = NeonCyan,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "CURO X1",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        // Connection Pill (Glass)
        Surface(
            onClick = onConnectToggle,
            shape = RoundedCornerShape(50),
            color = Color.Transparent,
            border = androidx.compose.foundation.BorderStroke(1.dp, if (connectionState == DeviceConnectionState.CONNECTED) NeonLime else Color.Gray),
            modifier = Modifier.height(36.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (connectionState == DeviceConnectionState.CONNECTED) NeonLime else Color.Gray, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (connectionState == DeviceConnectionState.CONNECTED) "ONLINE" else "OFFLINE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = if (connectionState == DeviceConnectionState.CONNECTED) NeonLime else Color.Gray
                )
            }
        }
    }
}

@Composable
fun DisconnectPlaceholder(onConnect: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("SYSTEM STANDBY", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onConnect,
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.2f), contentColor = NeonCyan),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
        ) {
            Text("INITIALIZE LINK")
        }
    }
}

@Composable
fun DashboardGrid(onSelect: (MeasurementType) -> Unit) {
    val devices = listOf(
        DeviceCardData(
            name = "Thermometer",
            type = MeasurementType.THERMOMETER,
            accentColor = NeonCyan,
            reading = "36.8°C",
            statusBadge = "Ready",
            icon = DeviceIcon.THERMOMETER
        ),
        DeviceCardData(
            name = "Pulse Oximeter",
            type = MeasurementType.OXIMETER,
            accentColor = NeonRose,
            reading = "SpO₂ 98%",
            subReading = "72 BPM",
            statusBadge = null,
            icon = DeviceIcon.OXIMETER
        ),
        DeviceCardData(
            name = "BP Monitor",
            type = MeasurementType.BP_MONITOR,
            accentColor = NeonPurple,
            reading = "120/80",
            statusBadge = null,
            icon = DeviceIcon.BP_MONITOR
        ),
        DeviceCardData(
            name = "Otoscope",
            type = MeasurementType.OTOSCOPE,
            accentColor = NeonLime,
            reading = null,
            statusBadge = "Live View",
            icon = DeviceIcon.OTOSCOPE
        )
    )
    
    val stethoscope = DeviceCardData(
        name = "Stethoscope",
        type = MeasurementType.STETHOSCOPE,
        accentColor = NeonCyan,
        reading = null,
        statusBadge = "Heart Mode",
        icon = DeviceIcon.STETHOSCOPE
    )

    Column {
        SectionHeader("Select Module")
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(devices.size) { index ->
                val device = devices[index]
                PremiumDeviceCard(device = device, onClick = { onSelect(device.type) }, index = index)
            }
            
            // Full-width Stethoscope card
            item(span = { GridItemSpan(2) }) {
                StethoscopeCard(device = stethoscope, onClick = { onSelect(stethoscope.type) })
            }
        }
    }
}

enum class DeviceIcon {
    THERMOMETER, OXIMETER, BP_MONITOR, OTOSCOPE, STETHOSCOPE
}

data class DeviceCardData(
    val name: String,
    val type: MeasurementType,
    val accentColor: Color,
    val reading: String?,
    val subReading: String? = null,
    val statusBadge: String?,
    val icon: DeviceIcon
)

@Composable
fun PremiumDeviceCard(device: DeviceCardData, onClick: () -> Unit, index: Int = 0) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = GlassSurface.copy(alpha = 0.6f),
        modifier = Modifier
            .height(140.dp)
            .scaleOnPress() // Scale animation on tap
            .slideUpOnAppear(delayMillis = index * 100) // Staggered entrance
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        device.accentColor.copy(alpha = 0.5f),
                        device.accentColor.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon
            DeviceIconView(icon = device.icon, color = device.accentColor)
            
            // Name and Reading
            Column {
                Text(
                    text = device.name.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = TextWhite
                )
                
                if (device.reading != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Pulsing live indicator
                        PulsingDot(color = device.accentColor, size = 6)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = device.reading,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = device.accentColor,
                            modifier = Modifier.pulseGlow(device.accentColor, minAlpha = 0.8f, maxAlpha = 1f)
                        )
                    }
                    if (device.subReading != null) {
                        Text(
                            text = device.subReading,
                            style = MaterialTheme.typography.bodySmall,
                            color = device.accentColor.copy(alpha = 0.8f)
                        )
                    }
                }
                
                if (device.statusBadge != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    StatusBadge(text = device.statusBadge, color = device.accentColor)
                }
            }
        }
    }
}

@Composable
fun StethoscopeCard(device: DeviceCardData, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = GlassSurface.copy(alpha = 0.6f),
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .scaleOnPress() // Scale animation on tap
            .slideUpOnAppear(delayMillis = 400) // Appears after device cards
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        device.accentColor.copy(alpha = 0.5f),
                        device.accentColor.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Icon and Name
            Column {
                DeviceIconView(icon = device.icon, color = device.accentColor, size = 36)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = device.name.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = TextWhite
                )
            }
            
            // Center: Waveform Animation
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .padding(horizontal = 16.dp)
            ) {
                AudioWaveform(color = device.accentColor)
            }
            
            // Right: Status Badge
            if (device.statusBadge != null) {
                StatusBadge(text = device.statusBadge, color = device.accentColor)
            }
        }
    }
}

@Composable
fun AudioWaveform(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        
        val path = Path()
        path.moveTo(0f, centerY)
        
        for (x in 0..width.toInt() step 2) {
            val normalizedX = x / width
            val y = centerY + sin((normalizedX * 4 * Math.PI + phase).toDouble()).toFloat() * (height / 3) *
                    (0.5f + 0.5f * sin((normalizedX * 2 * Math.PI + phase * 0.5f).toDouble()).toFloat())
            path.lineTo(x.toFloat(), y)
        }
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun StatusBadge(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f),
        modifier = Modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun DeviceIconView(icon: DeviceIcon, color: Color, size: Int = 28) {
    Canvas(modifier = Modifier.size(size.dp)) {
        when (icon) {
            DeviceIcon.THERMOMETER -> {
                // Thermometer icon
                val strokeWidth = 2.dp.toPx()
                drawCircle(
                    color = color,
                    radius = size.dp.toPx() * 0.15f,
                    center = Offset(this.size.width / 2, this.size.height * 0.8f),
                    style = Stroke(width = strokeWidth)
                )
                drawLine(
                    color = color,
                    start = Offset(this.size.width / 2, this.size.height * 0.15f),
                    end = Offset(this.size.width / 2, this.size.height * 0.65f),
                    strokeWidth = strokeWidth
                )
            }
            DeviceIcon.OXIMETER -> {
                // Heart with pulse
                val strokeWidth = 2.dp.toPx()
                drawCircle(
                    color = color,
                    radius = size.dp.toPx() * 0.35f,
                    center = Offset(this.size.width / 2, this.size.height / 2),
                    style = Stroke(width = strokeWidth)
                )
                // Pulse line
                drawLine(
                    color = color,
                    start = Offset(this.size.width * 0.2f, this.size.height / 2),
                    end = Offset(this.size.width * 0.4f, this.size.height / 2),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = color,
                    start = Offset(this.size.width * 0.4f, this.size.height / 2),
                    end = Offset(this.size.width * 0.5f, this.size.height * 0.3f),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = color,
                    start = Offset(this.size.width * 0.5f, this.size.height * 0.3f),
                    end = Offset(this.size.width * 0.6f, this.size.height * 0.7f),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = color,
                    start = Offset(this.size.width * 0.6f, this.size.height * 0.7f),
                    end = Offset(this.size.width * 0.8f, this.size.height / 2),
                    strokeWidth = strokeWidth
                )
            }
            DeviceIcon.BP_MONITOR -> {
                // Blood pressure cuff icon
                val strokeWidth = 2.dp.toPx()
                drawRoundRect(
                    color = color,
                    topLeft = Offset(this.size.width * 0.2f, this.size.height * 0.2f),
                    size = androidx.compose.ui.geometry.Size(
                        this.size.width * 0.6f,
                        this.size.height * 0.6f
                    ),
                    style = Stroke(width = strokeWidth),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                )
                // Gauge needle
                drawLine(
                    color = color,
                    start = Offset(this.size.width / 2, this.size.height * 0.35f),
                    end = Offset(this.size.width / 2, this.size.height * 0.65f),
                    strokeWidth = strokeWidth
                )
            }
            DeviceIcon.OTOSCOPE -> {
                // Otoscope/camera icon
                val strokeWidth = 2.dp.toPx()
                drawCircle(
                    color = color,
                    radius = size.dp.toPx() * 0.3f,
                    center = Offset(this.size.width / 2, this.size.height / 2),
                    style = Stroke(width = strokeWidth)
                )
                drawCircle(
                    color = color,
                    radius = size.dp.toPx() * 0.15f,
                    center = Offset(this.size.width / 2, this.size.height / 2),
                    style = Stroke(width = strokeWidth)
                )
            }
            DeviceIcon.STETHOSCOPE -> {
                // Stethoscope icon
                val strokeWidth = 2.dp.toPx()
                // Head
                drawCircle(
                    color = color,
                    radius = size.dp.toPx() * 0.2f,
                    center = Offset(this.size.width / 2, this.size.height * 0.75f),
                    style = Stroke(width = strokeWidth)
                )
                // Tubes
                drawLine(
                    color = color,
                    start = Offset(this.size.width * 0.35f, this.size.height * 0.6f),
                    end = Offset(this.size.width * 0.35f, this.size.height * 0.2f),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = color,
                    start = Offset(this.size.width * 0.65f, this.size.height * 0.6f),
                    end = Offset(this.size.width * 0.65f, this.size.height * 0.2f),
                    strokeWidth = strokeWidth
                )
            }
        }
    }
}

@Composable
fun MeasurementContainer(
    type: MeasurementType,
    vitals: VitalsPacket,
    activeMeasurement: MeasurementType,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Column {
        TextButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) {
            Text("← BACK TO MODULES", color = TextGrey)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically { it + 50 } + fadeIn()
        ) {
            when (type) {
                MeasurementType.THERMOMETER -> ThermometerScreen(
                    vitals = if (vitals.type == type) vitals else VitalsPacket(type), 
                    onStartMeasure = onStart,
                    onStopMeasure = onStop
                )
                MeasurementType.OXIMETER -> OximeterScreen(
                    vitals = if (vitals.type == type) vitals else VitalsPacket(type),
                    isMeasuring = activeMeasurement == type,
                    onStartMeasure = onStart,
                    onStopMeasure = onStop
                )
                MeasurementType.BP_MONITOR -> BpMonitorScreen(
                    vitals = if (vitals.type == type) vitals else VitalsPacket(type),
                    isMeasuring = activeMeasurement == type,
                    onStartMeasure = onStart,
                    onStopMeasure = onStop
                )
                MeasurementType.OTOSCOPE -> OtoscopeScreen(
                    vitals = if (vitals.type == type) vitals else VitalsPacket(type),
                    isMeasuring = activeMeasurement == type,
                    onStartMeasure = onStart,
                    onStopMeasure = onStop
                )
                MeasurementType.STETHOSCOPE -> StethoscopeScreen(
                    vitals = if (vitals.type == type) vitals else VitalsPacket(type),
                    isMeasuring = activeMeasurement == type,
                    onStartMeasure = onStart,
                    onStopMeasure = onStop
                )
                else -> Box {}
            }
        }
    }
}
