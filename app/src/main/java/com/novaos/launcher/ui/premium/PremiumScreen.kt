package com.novaos.launcher.ui.premium

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novaos.launcher.core.billing.BillingManager
import com.novaos.launcher.domain.model.LauncherSettings
import com.novaos.launcher.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    val billingManager: BillingManager
) : ViewModel() {

    val settingsState: StateFlow<LauncherSettings> = settingsRepository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LauncherSettings()
        )

    fun purchaseSimulated(productId: String) {
        viewModelScope.launch {
            billingManager.simulatePurchase(productId)
        }
    }

    fun resetSimulated() {
        viewModelScope.launch {
            billingManager.simulateReset()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onBack: () -> Unit,
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val settings by viewModel.settingsState.collectAsState()
    val isDark = when (settings.themeMode) {
        com.novaos.launcher.domain.model.ThemeMode.LIGHT -> false
        com.novaos.launcher.domain.model.ThemeMode.DARK -> true
        com.novaos.launcher.domain.model.ThemeMode.AUTO -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    val context = LocalContext.current
    val activity = context as? Activity

    val primaryColor = Color(settings.accentColor)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDark) Color(0xFF0F0F12) else Color(0xFFF2F2F7)
            )
            .systemBarsPadding()
    ) {
        // Gradient backdrop header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = primaryColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NovaOS Launcher Pro",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isDark) Color.White else Color.Black
                )
            }

            // Crown / Shield Logo
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp, bottom = 12.dp)
                    .size(80.dp)
                    .shadow(8.dp, CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(primaryColor, primaryColor.copy(alpha = 0.7f))
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Pro",
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }

            Text(
                text = "Upgrade to Pro",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                color = if (isDark) Color.White else Color.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Unlock the ultimate launcher customization",
                fontSize = 14.sp,
                color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Premium benefits cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BenefitRow(
                    title = "Passcode Security (App Lock & Hide)",
                    description = "Protect your apps with a 4-digit PIN, or hide them entirely from your drawer.",
                    accentColor = primaryColor,
                    isDark = isDark
                )
                BenefitRow(
                    title = "Custom Grid Configuration",
                    description = "Freely change rows and columns up to 5x7 to create your perfect setup.",
                    accentColor = primaryColor,
                    isDark = isDark
                )
                BenefitRow(
                    title = "Custom Shape Corner Sizing",
                    description = "Adjust shape dimensions and launcher icon shapes dynamically.",
                    accentColor = primaryColor,
                    isDark = isDark
                )
                BenefitRow(
                    title = "100% Ad-Free Settings",
                    description = "Remove all premium update promotion cards and banner ads from the menus.",
                    accentColor = primaryColor,
                    isDark = isDark
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Pro tier selection
            if (settings.isPremium) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF34C759).copy(alpha = 0.15f))
                        .border(1.5.dp, Color(0xFF34C759), RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Active",
                            tint = Color(0xFF34C759),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Premium Version Active",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF34C759)
                        )
                        Text(
                            text = "Thank you for supporting the development of NovaOS Launcher!",
                            fontSize = 12.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Test reset button
                        TextButton(onClick = { viewModel.resetSimulated() }) {
                            Text("Reset Pro (For testing purposes)", color = Color.Red, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Option 1: Lifetime Pro
                    PurchaseOptionCard(
                        title = "Lifetime Premium",
                        price = "$4.99",
                        subtitle = "One-time payment. Forever yours.",
                        isBestValue = true,
                        primaryColor = primaryColor,
                        isDark = isDark,
                        onClick = {
                            if (activity != null) {
                                viewModel.billingManager.launchPurchaseFlow(activity, "lifetime_pro", "inapp")
                            } else {
                                viewModel.purchaseSimulated("lifetime_pro")
                            }
                        }
                    )

                    // Option 2: Monthly Sub
                    PurchaseOptionCard(
                        title = "Monthly Premium",
                        price = "$0.99 / mo",
                        subtitle = "Billed monthly. Cancel anytime.",
                        isBestValue = false,
                        primaryColor = primaryColor,
                        isDark = isDark,
                        onClick = {
                            if (activity != null) {
                                viewModel.billingManager.launchPurchaseFlow(activity, "monthly_premium", "subs")
                            } else {
                                viewModel.purchaseSimulated("monthly_premium")
                            }
                        }
                    )

                    // Simulated Upgrade Button for direct testing
                    Button(
                        onClick = { viewModel.purchaseSimulated("lifetime_pro") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        )
                    ) {
                        Text(
                            text = "Simulate Instant Buy (Test Bypass)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun BenefitRow(
    title: String,
    description: String,
    accentColor: Color,
    isDark: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Benefit",
            tint = accentColor,
            modifier = Modifier.size(24.dp)
        )
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (isDark) Color.White else Color.Black
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun PurchaseOptionCard(
    title: String,
    price: String,
    subtitle: String,
    isBestValue: Boolean,
    primaryColor: Color,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val cardBg = if (isDark) Color(0xFF1C1C1E) else Color.White
    val borderModifier = if (isBestValue) {
        Modifier.border(2.dp, primaryColor, RoundedCornerShape(16.dp))
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(borderModifier)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isDark) Color.White else Color.Black
                    )
                    if (isBestValue) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(primaryColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "BEST VALUE",
                                color = primaryColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Text(
                text = price,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = primaryColor
            )
        }
    }
}
