# Curotel - Medical Device Management App

A modern Android application for managing and monitoring medical devices including thermometers, oximeters, blood pressure monitors, and otoscopes.

## 📱 Features

- **Dashboard Screen**: Central hub to view and manage all connected medical devices
- **Device Management**: Monitor thermometers, oximeters, BP monitors, and otoscopes
- **Video Splash Screen**: Premium video-based splash screen for a polished app launch experience
- **Dark Theme**: Modern dark theme with neon accents for a premium "medical device" feel

---

## 🎬 Video Splash Screen Implementation

### Overview
The app features a full-screen video splash screen that plays on app launch, providing a premium and immersive user experience before transitioning to the main dashboard.

### Technical Details

| Component | Technology |
|-----------|------------|
| Video Player | Media3 ExoPlayer (v1.5.1) |
| UI Framework | Jetpack Compose |
| Video Format | MP4 (1280x720, 16:9) |

### Files Modified/Created

#### 1. Dependencies Added
**`gradle/libs.versions.toml`**
```toml
media3 = "1.5.1"

[libraries]
androidx-media3-exoplayer = { group = "androidx.media3", name = "media3-exoplayer", version.ref = "media3" }
androidx-media3-ui = { group = "androidx.media3", name = "media3-ui", version.ref = "media3" }
```

**`app/build.gradle.kts`**
```kotlin
// Media3 ExoPlayer for video splash screen
implementation(libs.androidx.media3.exoplayer)
implementation(libs.androidx.media3.ui)
```

#### 2. Splash Screen Component
**`app/src/main/java/com/app/curotel/ui/splash/SplashScreen.kt`**

A Jetpack Compose composable that:
- Uses Media3 ExoPlayer for video playback
- Displays video with horizontal margins (24dp) and vertical margins (100dp)
- Features rounded corners (16dp) for a premium look
- Plays video at **1.5x speed** for faster splash experience
- Uses `RESIZE_MODE_FIT` to show full video content
- Black background for seamless loading
- Automatically navigates to dashboard when video completes

#### 3. MainActivity Integration
**`app/src/main/java/com/app/curotel/MainActivity.kt`**

Updated to:
- Show `SplashScreen` first on app launch
- Track splash completion state
- Transition to `DashboardScreen` after video finishes

#### 4. Video Resource
**`app/src/main/res/raw/mobile_screen_splash_screen_video.mp4`**
- Renamed from `Mobile_Screen_Splash_Screen_Video.mp4` to follow Android naming conventions (lowercase with underscores)
- Dimensions: 1280x720 (16:9 landscape)
- Size: ~779 KB

### Splash Screen Configuration

| Setting | Value | Description |
|---------|-------|-------------|
| Horizontal Margins | 24dp | Left and right padding |
| Vertical Margins | 100dp | Top and bottom padding |
| Corner Radius | 16dp | Rounded corners for premium look |
| Playback Speed | 1.5x | Faster video playback |
| Resize Mode | FIT | Shows full video content |
| Background | Black | Seamless loading experience |

### Code Structure

```
app/src/main/java/com/app/curotel/
├── MainActivity.kt                    # Entry point with splash flow
├── ui/
│   ├── splash/
│   │   └── SplashScreen.kt           # Video splash screen
│   ├── dashboard/
│   │   └── DashboardScreen.kt        # Main dashboard
│   ├── thermometer/
│   ├── oximeter/
│   ├── bpmonitor/
│   ├── otoscope/
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── viewmodel/
├── domain/
├── data/
└── core/

app/src/main/res/
└── raw/
    └── mobile_screen_splash_screen_video.mp4
```

---

## 🛠️ Build & Run

### Prerequisites
- Android Studio Hedgehog or later
- JDK 11+
- Android SDK 27+ (minSdk)

### Build Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean assembleDebug
```

### Run on Device
1. Connect an Android device or start an emulator
2. Run from Android Studio or use:
```bash
./gradlew installDebug
```

---

## 🎨 Theme

The app uses a dark theme with neon accents:

| Color | Hex | Usage |
|-------|-----|-------|
| Deep Space Black | Background | Main background color |
| Neon Cyan | Primary | Primary UI elements |
| Neon Purple | Secondary | Accent elements |
| Neon Lime | Tertiary | Highlights |
| Glass Surface | Surface | Card backgrounds |
| Text White | On Background | Text color |

---

## 📋 Requirements

- **minSdk**: 27 (Android 8.1 Oreo)
- **targetSdk**: 36
- **compileSdk**: 36

---

## 📝 Changelog

### Version 1.0 (2026-01-19)

#### Splash Screen Implementation
- ✅ Added Media3 ExoPlayer dependency (v1.5.1)
- ✅ Created `SplashScreen.kt` with video playback
- ✅ Integrated splash flow in `MainActivity.kt`
- ✅ Renamed video file to Android-compatible format
- ✅ Added horizontal margins (24dp) for proper mobile display
- ✅ Added vertical margins (100dp) for centered appearance
- ✅ Added rounded corners (16dp) for premium look
- ✅ Increased playback speed to 1.5x

---

## 📄 License

Copyright © 2026 Curotel. All rights reserved.
