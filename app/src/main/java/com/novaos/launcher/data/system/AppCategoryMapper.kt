package com.novaos.launcher.data.system

import android.content.pm.ApplicationInfo
import com.novaos.launcher.domain.model.AppCategory

/**
 * Maps Android's app category integers to our AppCategory enum.
 */
object AppCategoryMapper {

    fun mapCategory(appInfo: ApplicationInfo): AppCategory {
        return when (appInfo.category) {
            ApplicationInfo.CATEGORY_SOCIAL -> AppCategory.SOCIAL
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> AppCategory.PRODUCTIVITY
            ApplicationInfo.CATEGORY_GAME -> AppCategory.GAMES
            ApplicationInfo.CATEGORY_NEWS -> AppCategory.NEWS
            ApplicationInfo.CATEGORY_AUDIO -> AppCategory.MUSIC
            ApplicationInfo.CATEGORY_VIDEO -> AppCategory.ENTERTAINMENT
            ApplicationInfo.CATEGORY_IMAGE -> AppCategory.PHOTOGRAPHY
            ApplicationInfo.CATEGORY_MAPS -> AppCategory.TRAVEL
            else -> guessCategory(appInfo.packageName)
        }
    }

    /**
     * Fallback category guessing based on package name patterns.
     */
    private fun guessCategory(packageName: String): AppCategory {
        val pkg = packageName.lowercase()
        return when {
            // Social
            pkg.contains("facebook") || pkg.contains("instagram") ||
            pkg.contains("twitter") || pkg.contains("snapchat") ||
            pkg.contains("tiktok") || pkg.contains("linkedin") ||
            pkg.contains("whatsapp") || pkg.contains("telegram") ||
            pkg.contains("discord") || pkg.contains("signal") -> AppCategory.SOCIAL

            // Communication
            pkg.contains("messenger") || pkg.contains("sms") ||
            pkg.contains("dialer") || pkg.contains("phone") ||
            pkg.contains("contacts") || pkg.contains("email") ||
            pkg.contains("mail") -> AppCategory.COMMUNICATION

            // Games
            pkg.contains("game") || pkg.contains("play.") -> AppCategory.GAMES

            // Finance
            pkg.contains("bank") || pkg.contains("pay") ||
            pkg.contains("wallet") || pkg.contains("finance") ||
            pkg.contains("money") || pkg.contains("paytm") ||
            pkg.contains("gpay") || pkg.contains("phonepe") -> AppCategory.FINANCE

            // Shopping
            pkg.contains("shop") || pkg.contains("amazon") ||
            pkg.contains("flipkart") || pkg.contains("myntra") ||
            pkg.contains("ajio") || pkg.contains("meesho") -> AppCategory.SHOPPING

            // Entertainment
            pkg.contains("netflix") || pkg.contains("youtube") ||
            pkg.contains("hotstar") || pkg.contains("prime") ||
            pkg.contains("spotify") || pkg.contains("music") -> AppCategory.ENTERTAINMENT

            // Tools
            pkg.contains("calculator") || pkg.contains("clock") ||
            pkg.contains("calendar") || pkg.contains("weather") ||
            pkg.contains("flashlight") || pkg.contains("file") ||
            pkg.contains("settings") -> AppCategory.TOOLS

            // Photography
            pkg.contains("camera") || pkg.contains("photo") ||
            pkg.contains("gallery") || pkg.contains("snapseed") -> AppCategory.PHOTOGRAPHY

            // Education
            pkg.contains("learn") || pkg.contains("edu") ||
            pkg.contains("study") || pkg.contains("school") -> AppCategory.EDUCATION

            // Health
            pkg.contains("health") || pkg.contains("fitness") ||
            pkg.contains("workout") || pkg.contains("step") -> AppCategory.HEALTH

            // News
            pkg.contains("news") || pkg.contains("inshorts") -> AppCategory.NEWS

            // Travel
            pkg.contains("map") || pkg.contains("uber") ||
            pkg.contains("ola") || pkg.contains("travel") ||
            pkg.contains("booking") -> AppCategory.TRAVEL

            // Food
            pkg.contains("food") || pkg.contains("zomato") ||
            pkg.contains("swiggy") || pkg.contains("delivery") -> AppCategory.FOOD

            // Productivity
            pkg.contains("docs") || pkg.contains("sheets") ||
            pkg.contains("office") || pkg.contains("note") ||
            pkg.contains("todo") || pkg.contains("task") -> AppCategory.PRODUCTIVITY

            else -> AppCategory.OTHER
        }
    }
}
