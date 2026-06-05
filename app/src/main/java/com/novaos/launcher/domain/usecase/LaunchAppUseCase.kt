package com.novaos.launcher.domain.usecase

import android.content.Context
import android.content.Intent
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Use case for launching an installed app by package name.
 */
class LaunchAppUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Launch the app with the given package name.
     * Returns true if launch was successful, false otherwise.
     */
    operator fun invoke(packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                Toast.makeText(context, "App not found", Toast.LENGTH_SHORT).show()
                false
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to launch app", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
