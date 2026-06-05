package com.novaos.launcher.data.system

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import com.novaos.launcher.domain.model.AppCategory
import com.novaos.launcher.domain.model.AppInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * System source that fetches installed apps from Android PackageManager.
 */
@Singleton
class PackageManagerSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val packageManager: PackageManager = context.packageManager

    /**
     * Get all launchable apps installed on the device.
     */
    fun getInstalledApps(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }

        return resolveInfos
            .filter { it.activityInfo.packageName != context.packageName } // Exclude self
            .map { resolveInfo ->
                val appInfo = resolveInfo.activityInfo.applicationInfo
                val packageName = resolveInfo.activityInfo.packageName

                // Get install/update time
                val packageInfo = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        packageManager.getPackageInfo(
                            packageName,
                            PackageManager.PackageInfoFlags.of(0)
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager.getPackageInfo(packageName, 0)
                    }
                } catch (e: Exception) {
                    null
                }

                // Determine category
                val category = if (appInfo != null) {
                    AppCategoryMapper.mapCategory(appInfo)
                } else {
                    AppCategory.OTHER
                }

                AppInfo(
                    packageName = packageName,
                    label = resolveInfo.loadLabel(packageManager).toString(),
                    icon = resolveInfo.loadIcon(packageManager),
                    category = category,
                    installedAt = packageInfo?.firstInstallTime ?: 0L,
                    updatedAt = packageInfo?.lastUpdateTime ?: 0L
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    /**
     * Get a single app's info.
     */
    fun getAppInfo(packageName: String): AppInfo? {
        return try {
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.ApplicationInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(packageName, 0)
            }

            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }

            AppInfo(
                packageName = packageName,
                label = appInfo.loadLabel(packageManager).toString(),
                icon = appInfo.loadIcon(packageManager),
                category = AppCategoryMapper.mapCategory(appInfo),
                installedAt = packageInfo.firstInstallTime,
                updatedAt = packageInfo.lastUpdateTime
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    /**
     * Get default dock app package names.
     */
    fun getDefaultDockApps(): List<String> {
        val dockIntents = listOf(
            Intent(Intent.ACTION_DIAL),       // Phone
            Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_APP_MESSAGING) },  // Messages
            Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_APP_BROWSER) },    // Browser
            Intent("android.media.action.STILL_IMAGE_CAMERA")  // Camera
        )

        return dockIntents.mapNotNull { intent ->
            val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.resolveActivity(
                    intent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            }
            resolveInfo?.activityInfo?.packageName
        }.distinct()
    }
}
