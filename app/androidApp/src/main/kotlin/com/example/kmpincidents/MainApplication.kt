package com.example.kmpincidents

import android.app.Application
import com.example.kmpincidents.di.androidDataModule
import com.example.kmpincidents.di.dataModule
import com.example.kmpincidents.di.networkModule
import com.example.kmpincidents.di.viewModelModule
import com.example.kmpincidents.util.AndroidContextHolder
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidContextHolder.init(this)

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(
                networkModule,
                dataModule,
                androidDataModule,
                viewModelModule
            )
        }
    }
}