package com.novaos.launcher.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.novaos.launcher.domain.repository.AppRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BroadcastReceiver for handling package install/uninstall/update events.
 * Updates the app repository when apps change on the device.
 */
@AndroidEntryPoint
class PackageChangeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var appRepository: AppRepository

    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.data?.schemeSpecificPart ?: return

        // Skip self
        if (packageName == context.packageName) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    Intent.ACTION_PACKAGE_ADDED,
                    Intent.ACTION_PACKAGE_REPLACED,
                    Intent.ACTION_PACKAGE_CHANGED -> {
                        appRepository.onAppInstalled(packageName)
                    }
                    Intent.ACTION_PACKAGE_REMOVED -> {
                        val isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                        if (!isReplacing) {
                            appRepository.onAppUninstalled(packageName)
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
