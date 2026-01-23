package com.app.curotel.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.curotel.core.ui.scaleOnPress
import com.app.curotel.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.sin

data class OnboardingPage(
    val icon: String,
    val title: String,
    val description: String,
    val accentColor: Color
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            icon = "🏥",
            title = "Welcome to Curotel",
            description = "Revolutionizing healthcare with Industry 4.0 telemedicine solutions. Your health, anywhere, anytime.",
            accentColor = NeonCyan
        ),
        OnboardingPage(
            icon = "📱",
            title = "Meet CURO Device",
            description = "A multi-functional medical device that provides objective data for accurate remote diagnostics.",
            accentColor = NeonPurple
        ),
        OnboardingPage(
            icon = "🩺",
            title = "5 Devices in One",
            description = "Thermometer, Pulse Oximeter, BP Monitor, Otoscope, and Digital Stethoscope - all in your pocket.",
            accentColor = NeonRose
        ),
        OnboardingPage(
            icon = "👨‍⚕️",
            title = "Connect with Doctors",
            description = "Virtual consultations as easy as a WhatsApp call. Get checked within 5 minutes from anywhere.",
            accentColor = NeonLime
        ),
        OnboardingPage(
            icon = "🚀",
            title = "Ready to Start!",
            description = "Let's set up your CURO device and begin your journey to better healthcare access.",
            accentColor = NeonCyan
        )
    )
    
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpaceBlack)
    ) {
        // Animated background
        AnimatedBackground(
            color = pages[pagerState.currentPage].accentColor
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Skip button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onComplete) {
                    Text(
                        text = "SKIP",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextGrey
                    )
                }
            }
            
            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(
                    page = pages[page],
                    pageIndex = page
                )
            }
            
            // Page indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pages.size) { index ->
                    PageIndicator(
                        isSelected = pagerState.currentPage == index,
                        color = pages[index].accentColor
                    )
                    if (index < pages.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
            
            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                if (pagerState.currentPage > 0) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        modifier = Modifier.width(100.dp)
                    ) {
                        Text(
                            text = "← BACK",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextGrey
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(100.dp))
                }
                
                // Next/Get Started button
                Button(
                    onClick = {
                        if (pagerState.currentPage == pages.size - 1) {
                            onComplete()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .height(56.dp)
                        .scaleOnPress(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = pages[pagerState.currentPage].accentColor,
                        contentColor = DeepSpaceBlack
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage == pages.size - 1) "GET STARTED →" else "NEXT →",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    pageIndex: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated icon container
        val infiniteTransition = rememberInfiniteTransition(label = "iconFloat")
        val offsetY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 15f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "floatY"
        )
        
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(y = offsetY.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            page.accentColor.copy(alpha = 0.3f),
                            page.accentColor.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
                .border(2.dp, page.accentColor.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = page.icon,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp)
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Title
        Text(
            text = page.title.uppercase(),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = page.accentColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = TextWhite,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        
        // Feature highlights for specific pages
        if (pageIndex == 2) {
            Spacer(modifier = Modifier.height(32.dp))
            DeviceFeaturesList(page.accentColor)
        }
    }
}

@Composable
fun DeviceFeaturesList(accentColor: Color) {
    // Row 1: First 3 devices
    val row1 = listOf(
        "🌡️" to "Thermo",
        "❤️" to "Oximeter",
        "💜" to "BP"
    )
    
    // Row 2: Last 2 devices
    val row2 = listOf(
        "👁️" to "Otoscope",
        "🩺" to "Stethoscope"
    )
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // First row - 3 items
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            row1.forEachIndexed { index, (icon, name) ->
                DeviceChip(icon = icon, name = name, color = accentColor)
                if (index < row1.size - 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
        
        // Second row - 2 items
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            row2.forEachIndexed { index, (icon, name) ->
                DeviceChip(icon = icon, name = name, color = accentColor)
                if (index < row2.size - 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}

@Composable
fun DeviceChip(
    icon: String,
    name: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = TextWhite,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun PageIndicator(
    isSelected: Boolean,
    color: Color
) {
    val width by animateDpAsState(
        targetValue = if (isSelected) 24.dp else 8.dp,
        animationSpec = tween(300),
        label = "indicatorWidth"
    )
    
    Box(
        modifier = Modifier
            .height(8.dp)
            .width(width)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) color else color.copy(alpha = 0.3f))
    )
}

@Composable
fun AnimatedBackground(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "bgAnimation")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bgPhase"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // Draw subtle animated waves
        for (i in 0..2) {
            val path = Path()
            val waveHeight = height * 0.1f
            val yOffset = height * (0.3f + i * 0.2f)
            
            path.moveTo(0f, yOffset)
            
            for (x in 0..width.toInt() step 10) {
                val y = yOffset + sin((x / width * 2 * Math.PI + phase + i).toDouble()).toFloat() * waveHeight
                path.lineTo(x.toFloat(), y)
            }
            
            drawPath(
                path = path,
                color = color.copy(alpha = 0.05f - i * 0.01f),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}
