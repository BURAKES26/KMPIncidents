package com.example.kmpincidents.di

import com.example.kmpincidents.data.store.IncidentDataStore
import com.example.kmpincidents.data.store.TokenPreferences
import com.example.kmpincidents.util.PhotoFileResolver
import org.koin.dsl.module

val androidDataModule = module {
    single { TokenPreferences() }
    single { IncidentDataStore() }
    single { PhotoFileResolver() }
}
