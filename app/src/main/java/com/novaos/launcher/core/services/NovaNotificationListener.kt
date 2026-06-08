package com.novaos.launcher.core.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Service that listens for notifications to update app unread counts (badges).
 */
class NovaNotificationListener : NotificationListenerService() {

    companion object {
        private val _badgeCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
        val badgeCounts = _badgeCounts.asStateFlow()
        
        var isActive = false
            private set
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isActive = true
        updateAllCounts()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isActive = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        updateAllCounts()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        updateAllCounts()
    }

    private fun updateAllCounts() {
        val counts = activeNotifications
            .filter { !it.isOngoing } // Only count dismissible notifications
            .groupBy { it.packageName }
            .mapValues { (_, notifications) -> notifications.size }

        _badgeCounts.update { counts }
    }
}
