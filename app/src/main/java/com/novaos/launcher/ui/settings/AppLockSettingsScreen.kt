package com.novaos.launcher.ui.settings

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.layout.layout
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novaos.launcher.data.local.room.dao.HiddenAppDao
import com.novaos.launcher.data.local.room.entity.HiddenAppEntity
import com.novaos.launcher.domain.model.AppInfo
import com.novaos.launcher.domain.model.LauncherSettings
import com.novaos.launcher.domain.repository.AppRepository
import com.novaos.launcher.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppLockSettingsUiState(
    val settings: LauncherSettings = LauncherSettings(),
    val allApps: List<AppInfo> = emptyList(),
    val hiddenAppsList: List<HiddenAppEntity> = emptyList(),
    val searchQuery: String = ""
)

@HiltViewModel
class AppLockSettingsViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val hiddenAppDao: HiddenAppDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val uiState: StateFlow<AppLockSettingsUiState> = combine(
        settingsRepository.getSettings(),
        appRepository.getAllApps(),
        hiddenAppDao.getHiddenApps(),
        _searchQuery
    ) { settings, apps, hiddenList, query ->
        val filteredApps = if (query.isBlank()) {
            apps
        } else {
            apps.filter { it.label.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true) }
        }
        AppLockSettingsUiState(
            settings = settings,
            allApps = filteredApps,
            hiddenAppsList = hiddenList,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppLockSettingsUiState()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setPin(pin: String) {
        viewModelScope.launch {
            val current = uiState.value.settings
            settingsRepository.updateSettings(current.copy(appLockPin = pin))
        }
    }

    fun clearPin() {
        viewModelScope.launch {
            val current = uiState.value.settings
            settingsRepository.updateSettings(current.copy(appLockPin = null))
            // Remove all locked apps from db
            hiddenAppDao.deleteAll()
            // Unhide all hidden apps in app repository
            appRepository.getAllApps().first().forEach { app ->
                if (app.isHidden) {
                    appRepository.unhideApp(app.packageName)
                }
            }
        }
    }

    fun toggleAppHide(packageName: String, shouldHide: Boolean) {
        viewModelScope.launch {
            if (shouldHide) {
                // If we hide it, make sure it is not locked in hidden_apps
                appRepository.hideApp(packageName)
            } else {
                appRepository.unhideApp(packageName)
            }
        }
    }

    fun toggleAppLock(packageName: String, shouldLock: Boolean) {
        viewModelScope.launch {
            if (shouldLock) {
                // If locking, we must also ensure it's not hidden (isHidden = 0)
                appRepository.unhideApp(packageName) // sets isHidden = 0 in appDao
                // Then insert locked status in hidden_apps table
                hiddenAppDao.insertHiddenApp(HiddenAppEntity(packageName, locked = true))
            } else {
                hiddenAppDao.removeHiddenApp(packageName)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLockSettingsScreen(
    isDark: Boolean,
    primaryColor: Color,
    viewModel: AppLockSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val hasPin = !state.settings.appLockPin.isNullOrBlank()

    var showPinSetupDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // PIN Status card
        Text(
            text = "Passcode security",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f),
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )

        SettingsCard(isDark = isDark) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPinSetupDialog = true }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (hasPin) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = if (hasPin) primaryColor else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (hasPin) "Change Passcode" else "Set Passcode",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White else Color.Black
                        )
                        Text(
                            text = if (hasPin) "4-Digit PIN is active" else "Secure your apps with a PIN",
                            fontSize = 12.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }

            if (hasPin) {
                SettingsDivider(isDark = isDark)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.clearPin() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFFF453A),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Turn Passcode Off",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFF453A)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (hasPin) {
            // App lists title
            Text(
                text = "Lock or Hide Apps",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            // Search bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                placeholder = { Text("Search apps...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = if (isDark) Color(0xFF1C1C1E) else Color.White,
                    unfocusedContainerColor = if (isDark) Color(0xFF1C1C1E) else Color.White
                )
            )

            // Scrollable app settings rows
            SettingsCard(isDark = isDark) {
                if (state.allApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No apps found",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Column(modifier = Modifier.heightIn(max = 400.dp)) {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(state.allApps) { app ->
                                val isHidden = app.isHidden
                                val isLocked = state.hiddenAppsList.any { it.packageName == app.packageName && it.locked }
                                
                                AppLockRow(
                                    app = app,
                                    isHidden = isHidden,
                                    isLocked = isLocked,
                                    isDark = isDark,
                                    primaryColor = primaryColor,
                                    onHideChange = { viewModel.toggleAppHide(app.packageName, it) },
                                    onLockChange = { viewModel.toggleAppLock(app.packageName, it) }
                                )
                                SettingsDivider(isDark = isDark)
                            }
                        }
                    }
                }
            }
        } else {
            // Display notice to set PIN
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Please set a Passcode above to start locking or hiding apps.",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    }

    // PIN Setup Dialog
    if (showPinSetupDialog) {
        PinSetupDialog(
            isDark = isDark,
            primaryColor = primaryColor,
            onSave = { pin ->
                viewModel.setPin(pin)
                showPinSetupDialog = false
            },
            onDismiss = { showPinSetupDialog = false }
        )
    }
}

@Composable
private fun AppLockRow(
    app: AppInfo,
    isHidden: Boolean,
    isLocked: Boolean,
    isDark: Boolean,
    primaryColor: Color,
    onHideChange: (Boolean) -> Unit,
    onLockChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App icon placeholder/Coil (Since we don't load icons directly inside lazy list easily unless cached, we just use a small box with letter)
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(primaryColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = app.label.take(1).uppercase(),
                color = primaryColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // App Name
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color.White else Color.Black
            )
            Text(
                text = app.packageName,
                fontSize = 10.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }

        // Toggles
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Hide App option
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Hide", fontSize = 9.sp, color = Color.Gray)
                Switch(
                    checked = isHidden,
                    onCheckedChange = onHideChange,
                    modifier = Modifier.scale(0.75f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = primaryColor
                    )
                )
            }

            // Lock App option
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Lock", fontSize = 9.sp, color = Color.Gray)
                Switch(
                    checked = isLocked,
                    onCheckedChange = onLockChange,
                    modifier = Modifier.scale(0.75f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = primaryColor
                    )
                )
            }
        }
    }
}

@Composable
private fun PinSetupDialog(
    isDark: Boolean,
    primaryColor: Color,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pinValue by remember { mutableStateOf("") }
    var confirmValue by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isConfirming) "Confirm Passcode" else "Enter New Passcode",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (isDark) Color.White else Color.Black
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Type a 4-digit PIN using numbers.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = if (isConfirming) confirmValue else pinValue,
                    onValueChange = { input ->
                        val cleanInput = input.filter { it.isDigit() }.take(4)
                        if (isConfirming) {
                            confirmValue = cleanInput
                        } else {
                            pinValue = cleanInput
                        }
                        errorMsg = ""
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 8.sp
                    ),
                    modifier = Modifier.width(160.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMsg.isNotEmpty()) {
                    Text(
                        text = errorMsg,
                        color = Color(0xFFFF453A),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isConfirming) {
                        if (confirmValue.length != 4) {
                            errorMsg = "PIN must be exactly 4 digits"
                        } else if (confirmValue != pinValue) {
                            errorMsg = "PINs do not match"
                            isConfirming = false
                            confirmValue = ""
                            pinValue = ""
                        } else {
                            onSave(confirmValue)
                        }
                    } else {
                        if (pinValue.length != 4) {
                            errorMsg = "PIN must be exactly 4 digits"
                        } else {
                            isConfirming = true
                        }
                    }
                }
            ) {
                Text(
                    text = if (isConfirming) "Save" else "Next",
                    color = primaryColor,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = if (isDark) Color(0xFF2C2C2E) else Color.White
    )
}

// Simple Helper Extension for sizing/scaling
fun Modifier.scale(scale: Float) = this.then(
    Modifier.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout((placeable.width * scale).toInt(), (placeable.height * scale).toInt()) {
            placeable.placeWithLayer(0, 0) {
                scaleX = scale
                scaleY = scale
            }
        }
    }
)
