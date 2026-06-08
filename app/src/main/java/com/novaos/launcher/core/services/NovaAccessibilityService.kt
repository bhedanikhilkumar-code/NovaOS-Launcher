package com.novaos.launcher.core.services

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent

class NovaAccessibilityService : AccessibilityService() {

    companion object {
        private var instance: NovaAccessibilityService? = null

        /**
         * Checks if the Accessibility Service is active.
         */
        val isActive: Boolean
            get() = instance != null

        /**
         * Locks the screen using Accessibility Service global action.
         */
        fun lockScreen(): Boolean {
            return instance?.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN) ?: false
        }

        /**
         * Opens Accessibility Settings to enable the service.
         */
        fun openAccessibilitySettings(context: Context) {
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No-op
    }

    override fun onInterrupt() {
        // No-op
    }
}
