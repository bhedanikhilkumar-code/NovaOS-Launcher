package com.novaos.launcher.core.ads

import android.content.Context
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.novaos.launcher.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    fun initialize(context: Context) {
        try {
            MobileAds.initialize(context) {}
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Composable banner ad.
     * Hides itself if user is premium.
     * Shows a beautiful premium mock ad block in debug mode, or the real AdMob AdView.
     */
    @Composable
    fun AdBanner(
        onUpgradeClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val settings by settingsRepository.getSettings().collectAsState(initial = null)
        val isPremium = settings?.isPremium ?: false

        if (isPremium) {
            // Premium users see no ads!
            return
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            // For testing & local builds, show a premium-looking mock banner ad
            // that prompts the user to upgrade. This acts as both a placeholder and a monetization driver.
            MockAdBanner(onUpgradeClick = onUpgradeClick)
        }
    }

    @Composable
    private fun MockAdBanner(
        onUpgradeClick: () -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF2C2C35),
                            Color(0xFF1E1E24)
                        )
                    )
                )
                .clickable { onUpgradeClick() }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFF9500), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "AD",
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            text = "NovaOS Premium Upgrade",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Unlock all customization features & hide ads.",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Pro",
                        tint = Color(0xFFFFD60A),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "PRO",
                        color = Color(0xFFFFD60A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    /**
     * Renders a real AdView from AdMob (not active by default to avoid compile/runtime AdMob crashes in basic emulators).
     */
    @Composable
    fun RealAdMobBanner(
        context: Context,
        adUnitId: String = "ca-app-pub-3940256099942544/6300978111" // AdMob Test Unit ID
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            factory = { ctx ->
                FrameLayout(ctx).apply {
                    val adView = AdView(ctx).apply {
                        setAdSize(AdSize.BANNER)
                        this.adUnitId = adUnitId
                        loadAd(AdRequest.Builder().build())
                    }
                    addView(adView)
                }
            }
        )
    }
}
