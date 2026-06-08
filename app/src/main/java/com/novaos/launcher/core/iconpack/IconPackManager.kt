package com.novaos.launcher.core.iconpack

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import com.novaos.launcher.domain.model.IconPackInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import org.xmlpull.v1.XmlPullParser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconPackManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val packageManager: PackageManager = context.packageManager
    private var currentIconPack: String? = null
    private var iconMapping = mutableMapOf<String, String>()
    private var iconPackResources: Resources? = null

    /**
     * Get all installed icon packs on the device.
     */
    fun getInstalledIconPacks(): List<IconPackInfo> {
        val intent = Intent("com.novaos.launcher.THEMES") // Standard icon pack intent
        val resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA)
        
        // Also check for ADW themes as they are common
        val adwIntent = Intent("org.adw.launcher.THEMES")
        val adwResolveInfos = packageManager.queryIntentActivities(adwIntent, PackageManager.GET_META_DATA)

        val allPacks = (resolveInfos + adwResolveInfos).distinctBy { it.activityInfo.packageName }

        return allPacks.map { info ->
            IconPackInfo(
                packageName = info.activityInfo.packageName,
                label = info.loadLabel(packageManager).toString(),
                icon = info.loadIcon(packageManager)
            )
        }.sortedBy { it.label }
    }

    /**
     * Load the mapping from an icon pack's appfilter.xml.
     */
    fun loadIconPack(packageName: String?) {
        if (packageName == null) {
            currentIconPack = null
            iconMapping.clear()
            iconPackResources = null
            return
        }

        if (currentIconPack == packageName) return

        try {
            currentIconPack = packageName
            iconPackResources = packageManager.getResourcesForApplication(packageName)
            iconMapping.clear()

            val resId = iconPackResources?.getIdentifier("appfilter", "xml", packageName)
            if (resId != null && resId != 0) {
                val parser = iconPackResources?.getXml(resId)
                var eventType = parser?.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && parser?.name == "item") {
                        val componentName = parser.getAttributeValue(null, "component")
                        val drawableName = parser.getAttributeValue(null, "drawable")
                        if (componentName != null && drawableName != null) {
                            iconMapping[componentName] = drawableName
                        }
                    }
                    eventType = parser?.next()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            currentIconPack = null
        }
    }

    /**
     * Load an icon for a specific package from the current icon pack.
     */
    fun getIcon(packageName: String, defaultIcon: Drawable?): Drawable? {
        if (currentIconPack == null || iconPackResources == null) return defaultIcon

        // Component format: ComponentInfo{pkg/activity}
        // Simplified mapping: we often match by package name if exact component isn't found
        val drawableName = iconMapping.entries.find { it.key.contains(packageName) }?.value
            ?: return defaultIcon

        return try {
            val resId = iconPackResources?.getIdentifier(drawableName, "drawable", currentIconPack)
            if (resId != null && resId != 0) {
                iconPackResources?.getDrawable(resId, null)
            } else {
                defaultIcon
            }
        } catch (e: Exception) {
            defaultIcon
        }
    }
}
