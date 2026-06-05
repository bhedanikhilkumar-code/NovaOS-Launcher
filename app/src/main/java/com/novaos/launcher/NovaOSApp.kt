package com.novaos.launcher

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NovaOSApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            com.google.android.gms.ads.MobileAds.initialize(this) {}
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
