package com.example.kmpincidents

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.kmpincidents.di.androidDataModule
import com.example.kmpincidents.di.dataModule
import com.example.kmpincidents.di.networkModule
import com.example.kmpincidents.di.viewModelModule
import com.example.kmpincidents.notification.IncidentNotificationService
import com.example.kmpincidents.util.AndroidContextHolder
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        AndroidContextHolder.init(this)

        startKoin {
            androidContext(this@MainApplication)
            modules(networkModule, dataModule, androidDataModule, viewModelModule)
        }

        // Run as a foreground service so incident update notifications keep being
        // delivered even when the app is closed / in the background.
        val serviceIntent = Intent(this, IncidentNotificationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}