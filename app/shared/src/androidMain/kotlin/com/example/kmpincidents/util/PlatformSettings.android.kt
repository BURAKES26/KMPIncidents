package com.example.kmpincidents.util

import android.content.Intent
import android.provider.Settings

actual fun openLanguageSettings() {
    val context = AndroidContextHolder.appContext
    val intent = Intent(Settings.ACTION_LOCALE_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}