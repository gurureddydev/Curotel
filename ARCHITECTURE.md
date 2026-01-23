# 🏗️ Curotel Architecture Guide

## Professional Android Architecture with Best Practices

This document outlines the architectural patterns and technologies used in the Curotel app.

---

## 📚 Technology Stack

| Category | Technology | Purpose |
|----------|------------|---------|
| **DI** | Hilt | Dependency Injection |
| **Networking** | Retrofit + OkHttp | REST API calls |
| **Serialization** | Moshi | JSON parsing |
| **Database** | Room | Local SQLite storage |
| **Preferences** | DataStore | Key-value storage |
| **Async** | Coroutines + Flow | Async operations & reactive streams |
| **Real-time** | WebSockets (OkHttp) | Live data streaming |
| **UI** | Jetpack Compose | Declarative UI |
| **Navigation** | Navigation Compose | Screen navigation |
| **State** | StateFlow / SharedFlow | Reactive state management |

---

## 📂 Project Structure

```
app/src/main/java/com/app/curotel/
├── CurotelApplication.kt        # Hilt Application entry point
├── MainActivity.kt               # Main Activity with @AndroidEntryPoint
│
├── di/                          # Dependency Injection Modules
│   ├── DatabaseModule.kt         # Room database providers
│   ├── NetworkModule.kt          # Retrofit, OkHttp, Moshi providers
│   └── RepositoryModule.kt       # Repository bindings
│
├── data/                        # Data Layer
│   ├── local/
│   │   ├── CurotelDatabase.kt    # Room database
│   │   ├── dao/
│   │   │   └── MeasurementDao.kt # Room DAO with Flow queries
│   │   ├── entity/
│   │   │   └── MeasurementEntity.kt # Room entities
│   │   └── preferences/
│   │       └── UserPreferencesManager.kt # DataStore preferences
│   │
│   ├── remote/
│   │   ├── api/
│   │   │   └── CurotelApiService.kt # Retrofit API interface
│   │   ├── dto/
│   │   │   └── Dtos.kt           # Moshi data transfer objects
│   │   └── websocket/
│   │       └── VitalsWebSocketService.kt # WebSocket for real-time data
│   │
│   └── repository/
│       ├── MeasurementRepository.kt # Measurement repository
│       └── DeviceRepositoryImpl.kt  # Device repository with WebSocket
│
├── domain/                      # Domain Layer
│   └── model/
│       └── Models.kt             # Domain models
│
├── viewmodel/                   # ViewModel Layer
│   └── DeviceViewModel.kt        # @HiltViewModel with StateFlow
│
├── navigation/                  # Navigation
│   └── Navigation.kt             # NavHost and Screen routes
│
├── ui/                          # UI Layer
│   ├── theme/                    # Material3 theming
│   ├── components/               # Reusable UI components
│   ├── splash/                   # Splash screen
│   ├── onboarding/               # Onboarding flow
│   ├── dashboard/                # Main dashboard
│   ├── thermometer/              # Device screens
│   ├── oximeter/
│   ├── bpmonitor/
│   ├── otoscope/
│   ├── stethoscope/
│   ├── history/                  # Measurement history
│   ├── consult/                  # Video consultation
│   └── settings/                 # App settings
│
└── core/                        # Core utilities
    └── ui/
        ├── GlassCard.kt          # Premium UI components
        └── Animations.kt         # Animation utilities
```

---

## 🔧 Hilt Dependency Injection

### Application Class
```kotlin
@HiltAndroidApp
class CurotelApplication : Application()
```

### Activity
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // ViewModel is injected via hiltViewModel()
}
```

### ViewModel
```kotlin
@HiltViewModel
class DeviceViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val measurementRepository: MeasurementRepository,
    private val preferencesManager: UserPreferencesManager
) : ViewModel()
```

### Modules
- **DatabaseModule**: Provides Room database and DAOs
- **NetworkModule**: Provides Retrofit, OkHttp, Moshi
- **RepositoryModule**: Binds repository interfaces to implementations

---

## 🌐 Networking with Retrofit + OkHttp

### API Service
```kotlin
interface CurotelApiService {
    @GET("api/v1/measurements")
    suspend fun getMeasurements(): Response<ApiResponse<List<MeasurementDto>>>
    
    @POST("api/v1/measurements")
    suspend fun createMeasurement(@Body measurement: MeasurementDto): Response<ApiResponse<MeasurementDto>>
}
```

### OkHttp Configuration
- Logging interceptor (debug only)
- Auth header interceptor (ready for implementation)
- 30-second timeouts
- Retry on connection failure

---

## 🗄️ Room Database

### Entity
```kotlin
@Entity(tableName = "measurements")
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val temperature: Float? = null,
    val heartRate: Int? = null,
    // ...
)
```

### DAO with Flow
```kotlin
@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements ORDER BY timestamp DESC")
    fun getAllMeasurements(): Flow<List<MeasurementEntity>>
}
```

---

## 📡 Real-time Data with WebSocket

```kotlin
class VitalsWebSocketService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val moshi: Moshi
) {
    fun connectToVitalsStream(): Flow<VitalsStreamDto> = callbackFlow {
        // WebSocket connection with Flow emission
    }
}
```

---

## 💾 DataStore Preferences

```kotlin
class UserPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val isOnboardingComplete: Flow<Boolean> = dataStore.data
        .map { it[IS_ONBOARDING_COMPLETE] ?: false }
    
    suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { it[IS_ONBOARDING_COMPLETE] = complete }
    }
}
```

---

## 🔄 State Management with Flow

### ViewModel State
```kotlin
// StateFlow for UI state
val connectionState: StateFlow<DeviceConnectionState> = deviceRepository.connectionState

// SharedFlow for one-time events
private val _errorMessage = MutableSharedFlow<String>()
val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

// Collecting from repository
val measurements: StateFlow<List<MeasurementEntity>> = measurementRepository
    .getAllMeasurements()
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
```

---

## 🧪 Testing Ready

The architecture supports easy testing:
- **Unit Tests**: ViewModels with fake repositories
- **Integration Tests**: Repositories with in-memory Room
- **UI Tests**: Compose testing with Hilt test runner

---

## 📋 Permissions

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

---

## 🚀 Next Steps

1. **Implement actual Bluetooth connectivity** using BluetoothLeScanner
2. **Add authentication** with JWT tokens in OkHttp interceptor
3. **Integrate video SDK** (Agora/WebRTC) for telemedicine
4. **Add offline-first sync** with WorkManager
5. **Implement push notifications** with Firebase Cloud Messaging
6. **Add CI/CD pipeline** with GitHub Actions

---

*Architecture Version: 2.0 | Last Updated: 2026-01-20*
