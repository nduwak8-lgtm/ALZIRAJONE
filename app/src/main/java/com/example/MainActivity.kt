package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.components.AlziraBottomNavBar
import com.example.ui.components.AlziraHeader
import com.example.ui.components.MoreMenuSheet
import com.example.ui.screens.*
import com.example.ui.theme.AlziraTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

class MainActivity : FragmentActivity() {

    private var pausedTimestamp: Long = 0
    private var mainViewModel: MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MainViewModel = viewModel()
            mainViewModel = viewModel

            AlziraTheme {
                AlziraAppRoot(
                    viewModel = viewModel,
                    onLaunchBiometric = { showBiometricPrompt() }
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        pausedTimestamp = System.currentTimeMillis()
    }

    override fun onResume() {
        super.onResume()
        val vm = mainViewModel ?: return
        val profile = vm.userProfile.value
        val timeout = profile.lockTimeoutSeconds

        if (timeout >= 0 && !profile.pinHash.isNullOrBlank() && pausedTimestamp > 0) {
            val elapsedSeconds = (System.currentTimeMillis() - pausedTimestamp) / 1000
            if (elapsedSeconds >= timeout) {
                vm.lockApp()
            }
        }
    }

    private fun showBiometricPrompt() {
        val vm = mainViewModel ?: return
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    vm.isAppUnlocked.value = true
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Alzira Joné")
            .setSubtitle("Desbloqueie com sua biometria")
            .setNegativeButtonText("Usar PIN")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}

@Composable
fun AlziraAppRoot(
    viewModel: MainViewModel,
    onLaunchBiometric: () -> Unit
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isAppUnlocked by viewModel.isAppUnlocked.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()

    val cycleStatus by viewModel.currentCycleStatus.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val selectedRecord by viewModel.selectedDateRecord.collectAsStateWithLifecycle()
    val outlook by viewModel.nextDaysOutlook.collectAsStateWithLifecycle()
    val tips by viewModel.dailyTips.collectAsStateWithLifecycle()
    val stats by viewModel.cycleStats.collectAsStateWithLifecycle()
    val allCycles by viewModel.allCycles.collectAsStateWithLifecycle()
    val allRecords by viewModel.allDailyRecords.collectAsStateWithLifecycle()
    val allMedications by viewModel.allMedications.collectAsStateWithLifecycle()
    val allReminders by viewModel.allReminders.collectAsStateWithLifecycle()
    val partnerSharing by viewModel.partnerSharing.collectAsStateWithLifecycle()

    var showMoreMenu by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Intercept back button to return to Dashboard unless already on Dashboard
    if (currentScreen != AppScreen.DASHBOARD && currentScreen != AppScreen.ONBOARDING) {
        BackHandler {
            viewModel.navigateTo(AppScreen.DASHBOARD)
        }
    }

    // 1. Onboarding check
    if (currentScreen == AppScreen.ONBOARDING || !userProfile.isOnboarded) {
        OnboardingScreen(
            onComplete = { name, age, habitualCycle, habitualPeriod, isRegular, lastPeriodDate, goal, pin ->
                viewModel.completeOnboarding(name, age, habitualCycle, habitualPeriod, isRegular, lastPeriodDate, goal, pin)
            }
        )
        return
    }

    // 2. Lock screen check
    val isPinSet = !userProfile.pinHash.isNullOrBlank()
    if (!isAppUnlocked && isPinSet) {
        LockScreen(
            customIconUri = userProfile.customIconUri ?: userProfile.avatarUri,
            isBiometricAvailable = userProfile.isBiometricEnabled,
            onUnlockWithPin = { pin -> viewModel.unlockApp(pin) },
            onBiometricUnlock = onLaunchBiometric
        )
        return
    }

    // 3. Main App Container
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_app_scaffold"),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            AlziraHeader(
                userName = userProfile.name,
                avatarUri = userProfile.customIconUri ?: userProfile.avatarUri,
                viewMode = viewMode,
                isPinProtected = isPinSet,
                onAvatarClick = { viewModel.navigateTo(AppScreen.ABOUT) },
                onModeClick = {
                    val nextMode = when (viewMode) {
                        "NORMAL" -> "PRIVATE"
                        "PRIVATE" -> "DISCREET"
                        else -> "NORMAL"
                    }
                    viewModel.setViewMode(nextMode)
                },
                onLockClick = { viewModel.lockApp() }
            )
        },
        bottomBar = {
            AlziraBottomNavBar(
                currentScreen = currentScreen,
                onNavigate = { screen ->
                    if (screen == AppScreen.MEDICATIONS) {
                        showMoreMenu = true
                    } else {
                        viewModel.navigateTo(screen)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentScreen) {
                AppScreen.DASHBOARD -> {
                    DashboardScreen(
                        status = cycleStatus,
                        todayRecord = selectedRecord,
                        outlook = outlook,
                        tips = tips,
                        viewMode = viewMode,
                        onNavigate = { screen -> viewModel.navigateTo(screen) },
                        onQuickFlow = { flow -> viewModel.logQuickFlow(flow) },
                        onQuickSymptom = { symptom -> viewModel.logQuickSymptom(symptom) },
                        onQuickMood = { mood -> viewModel.logQuickMood(mood) },
                        onQuickSexualActivity = { hadSex, isProtected, method ->
                            viewModel.logQuickSexualActivity(hadSex, isProtected, method)
                        }
                    )
                }
                AppScreen.CALENDAR -> {
                    CalendarScreen(
                        status = cycleStatus,
                        allRecords = allRecords,
                        selectedDate = selectedDate,
                        viewMode = viewMode,
                        onSelectDate = { date -> viewModel.selectDate(date) },
                        onNavigate = { screen -> viewModel.navigateTo(screen) }
                    )
                }
                AppScreen.DAILY_LOG -> {
                    DailyLogScreen(
                        currentDate = selectedDate,
                        existingRecord = selectedRecord,
                        viewMode = viewMode,
                        onDateChange = { date -> viewModel.selectDate(date) },
                        onSaveRecord = { record -> viewModel.saveDailyRecord(record) },
                        onBack = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                    )
                }
                AppScreen.STATISTICS -> {
                    StatisticsScreen(
                        stats = stats,
                        allCycles = allCycles,
                        allRecords = allRecords
                    )
                }
                AppScreen.MEDICATIONS -> {
                    MedicationsScreen(
                        medications = allMedications,
                        reminders = allReminders,
                        onToggleTaken = { med -> viewModel.toggleMedicationTaken(med) },
                        onDeleteMedication = { med -> viewModel.deleteMedication(med) },
                        onAddMedication = { name, dose, time, isContraceptive, type, notes ->
                            viewModel.addMedication(name, dose, time, isContraceptive, type, notes)
                        },
                        onToggleReminder = { rem -> viewModel.toggleReminder(rem) },
                        onDeleteReminder = { rem -> viewModel.deleteReminder(rem) },
                        onAddReminder = { title, message, time, isSecret, type ->
                            viewModel.addReminder(title, message, time, isSecret, type)
                        },
                        onTestNotification = { type -> viewModel.testNotification(type) }
                    )
                }
                AppScreen.REPORTS -> {
                    ReportsScreen(
                        profile = userProfile,
                        allCycles = allCycles,
                        allRecords = allRecords,
                        stats = stats
                    )
                }
                AppScreen.PARTNER -> {
                    PartnerScreen(
                        sharing = partnerSharing,
                        onUpdateSharing = { updated -> viewModel.updatePartnerSharing(updated) }
                    )
                }
                AppScreen.SETTINGS_SECURITY -> {
                    SecurityScreen(
                        profile = userProfile,
                        onSetPin = { pin -> viewModel.setPin(pin) },
                        onRemovePin = { viewModel.setPin("") },
                        onToggleBiometric = { enabled ->
                            viewModel.updateProfile(userProfile.copy(isBiometricEnabled = enabled))
                        },
                        onSetTimeout = { timeout ->
                            viewModel.updateProfile(userProfile.copy(lockTimeoutSeconds = timeout))
                        },
                        onSetViewMode = { mode -> viewModel.setViewMode(mode) },
                        onLockNow = { viewModel.lockApp() },
                        onClearAllData = { viewModel.clearAllUserData() }
                    )
                }
                AppScreen.ABOUT -> {
                    AboutScreen(
                        profile = userProfile,
                        onUpdateCustomIcon = { uri -> viewModel.updateCustomIcon(uri) },
                        onBackupData = { viewModel.backupDataToJson() },
                        onRestoreData = { json -> viewModel.restoreFromJson(json) }
                    )
                }
                AppScreen.ONBOARDING -> {
                    // Handled above
                }
            }
        }
    }

    if (showMoreMenu) {
        MoreMenuSheet(
            onDismiss = { showMoreMenu = false },
            onNavigate = { screen ->
                viewModel.navigateTo(screen)
            }
        )
    }
}
