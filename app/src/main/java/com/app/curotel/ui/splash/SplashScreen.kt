package com.app.curotel.ui.splash

import android.net.Uri
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.app.curotel.R

/**
 * Full-screen video splash screen using Media3 ExoPlayer.
 * 
 * The video is displayed with horizontal margins and rounded corners
 * for a polished, premium look on mobile devices.
 * 
 * @param onSplashComplete Callback invoked when the splash video finishes
 */
@OptIn(UnstableApi::class)
@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    val context = LocalContext.current
    var isVideoCompleted by remember { mutableStateOf(false) }
    
    // Build the URI for the raw video resource
    val videoUri = remember {
        Uri.parse("android.resource://${context.packageName}/${R.raw.mobile_screen_splash_screen_video}")
    }
    
    // Create and configure ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            repeatMode = Player.REPEAT_MODE_OFF
            playWhenReady = true
            // Play video at 1.5x speed for faster splash
            setPlaybackSpeed(1.5f)
            // Set volume to 0 if video has audio you don't want
            // volume = 0f
            prepare()
        }
    }
    
    // Listen for video completion
    LaunchedEffect(exoPlayer) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    isVideoCompleted = true
                }
            }
        })
    }
    
    // Navigate when video completes
    LaunchedEffect(isVideoCompleted) {
        if (isVideoCompleted) {
            onSplashComplete()
        }
    }
    
    // Release player when composable leaves composition
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    // Full-screen video container with margins
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // Hide playback controls
                    
                    // RESIZE_MODE_FIT: Scales video to fit within the view while 
                    // maintaining aspect ratio. The full video content is visible.
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    
                    // Seamless black background during loading
                    setBackgroundColor(android.graphics.Color.BLACK)
                    setShutterBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 100.dp) // Margins on left, right, top, bottom
                .clip(RoundedCornerShape(16.dp)) // Rounded corners for premium look
        )
    }
}
