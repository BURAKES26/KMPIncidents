package com.example.kmpincidents.util

import android.content.Intent
import android.provider.Settings

actual fun openLanguageSettings() {
    val intent = Intent(Settings.ACTION_LOCALE_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    AndroidContextHolder.appContext.startActivity(intent)
}